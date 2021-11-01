package io.papermc.hangar.service.internal.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigInteger;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

import io.papermc.hangar.config.hangar.HangarConfig;
import io.papermc.hangar.db.dao.internal.table.auth.UserOauthTokenDAO;
import io.papermc.hangar.db.dao.internal.table.auth.UserSignOnDAO;
import io.papermc.hangar.model.db.auth.UserOauthTokenTable;
import io.papermc.hangar.model.db.auth.UserSignOnTable;
import io.papermc.hangar.model.internal.sso.AuthUser;
import io.papermc.hangar.model.internal.sso.TokenResponce;
import io.papermc.hangar.model.internal.sso.Traits;
import io.papermc.hangar.model.internal.sso.URLWithNonce;
import io.papermc.hangar.security.authentication.HangarPrincipal;
import io.papermc.hangar.service.TokenService;

@Service
public class SSOService {

    private final HangarConfig hangarConfig;
    private final UserSignOnDAO userSignOnDAO;
    private final RestTemplate restTemplate;
    private final UserOauthTokenDAO userOauthTokenDAO;
    private final TokenService tokenService;

    @Autowired
    public SSOService(HangarConfig hangarConfig, UserSignOnDAO userSignOnDAO, RestTemplate restTemplate, UserOauthTokenDAO userOauthTokenDAO, TokenService tokenService) {
        this.hangarConfig = hangarConfig;
        this.userSignOnDAO = userSignOnDAO;
        this.restTemplate = restTemplate;
        this.userOauthTokenDAO = userOauthTokenDAO;
        this.tokenService = tokenService;
    }

    private boolean isNonceValid(String nonce) {
        UserSignOnTable userSignOn = userSignOnDAO.getByNonce(nonce);
        if (userSignOn == null) return false;
        long millisSinceCreated = userSignOn.getCreatedAt().until(OffsetDateTime.now(), ChronoUnit.MILLIS);
        if (userSignOn.isCompleted() || millisSinceCreated > 600000) return false;
        userSignOnDAO.markCompleted(userSignOn.getId());
        return true;
    }

    public URLWithNonce getLoginUrl(String returnUrl) {
        String generatedNonce = nonce();
        String url = UriComponentsBuilder.fromUriString(hangarConfig.sso.getOauthUrl() + hangarConfig.sso.getLoginUrl())
                .queryParam("client_id", hangarConfig.sso.getClientId())
                .queryParam("scope", "openid email profile")
                .queryParam("response_type", "code")
                .queryParam("redirect_uri", returnUrl)
                .queryParam("state", generatedNonce)
                .build().toUriString();

        return new URLWithNonce(url, generatedNonce);
    }

    private String idToken(String username) {
        UserOauthTokenTable token = userOauthTokenDAO.getByUsername(username);
        return token.getIdToken();
    }

    public URLWithNonce getLogoutUrl(String returnUrl, HangarPrincipal user) {
        String idToken = idToken(user.getName());
        String generatedNonce = nonce();
        String url = UriComponentsBuilder.fromUriString(hangarConfig.sso.getOauthUrl() + hangarConfig.sso.getLogoutUrl())
                .queryParam("post_logout_redirect_uri", returnUrl)
                .queryParam("id_token_hint", idToken)
                .queryParam("state", tokenService.simple(user.getName()))
                .build().toUriString();
        return new URLWithNonce(url, generatedNonce);
    }

    public String getSignupUrl(String returnUrl) {
        // TODO figure out what we wanna do here
        return hangarConfig.sso.getAuthUrl() + hangarConfig.sso.getSignupUrl();
    }

    private String nonce() {
        return new BigInteger(130, ThreadLocalRandom.current()).toString(32);
    }

    public AuthUser authenticate(String code, String nonce, String returnUrl) {
        String token = redeemCode(code, returnUrl);
        DecodedJWT decoded = JWT.decode(token);

        Traits traits = decoded.getClaim("traits").as(Traits.class);
//        long externalId = Long.parseLong(decoded.get("external_id"));
//        String avatarUrl = decoded.get("avatar_url");
        Locale language =  (traits.getLanguage() == null || traits.getLanguage().isBlank()) ? Locale.ENGLISH : Locale.forLanguageTag(traits.getLanguage());
//        String addGroups = decoded.get("add_groups");
        AuthUser authUser = new AuthUser(
                -5,
                traits.getUsername(),
                traits.getEmail(),
                "avatarUrl",
                language,
                traits.getName().getFirst() + traits.getName().getLast(),
                "addGroups"
        );
        if (!isNonceValid(nonce)) {
            return null;
        }
        saveIdToken(token, traits.getUsername());
        return authUser;
    }

    public String redeemCode(String code, String redirect) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("code", code);
        map.add("grant_type", "authorization_code");
        map.add("client_id", hangarConfig.sso.getClientId());
        map.add("redirect_uri", redirect);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);

        ResponseEntity<TokenResponce> tokenResponse = restTemplate.exchange(hangarConfig.sso.getOauthUrl() + hangarConfig.sso.getTokenUrl(), HttpMethod.POST, entity, TokenResponce.class);

        return tokenResponse.getBody().getIdToken();
    }

    public UserSignOnTable insert(String nonce) {
        return userSignOnDAO.insert(new UserSignOnTable(nonce));
    }

    public void saveIdToken(String idToken, String username) {
        userOauthTokenDAO.insert(new UserOauthTokenTable(username, idToken));
    }

    public void logout(String username) {
        userOauthTokenDAO.remove(username);
    }
}
