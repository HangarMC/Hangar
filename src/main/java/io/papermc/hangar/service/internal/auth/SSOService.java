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
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import io.papermc.hangar.config.hangar.HangarConfig;
import io.papermc.hangar.db.dao.internal.table.UserDAO;
import io.papermc.hangar.db.dao.internal.table.auth.UserOauthTokenDAO;
import io.papermc.hangar.db.dao.internal.table.auth.UserSignOnDAO;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.model.db.auth.UserOauthTokenTable;
import io.papermc.hangar.model.db.auth.UserSignOnTable;
import io.papermc.hangar.model.internal.sso.TokenResponse;
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
    private final UserDAO userDAO;

    @Autowired
    public SSOService(HangarConfig hangarConfig, UserSignOnDAO userSignOnDAO, RestTemplate restTemplate, UserOauthTokenDAO userOauthTokenDAO, TokenService tokenService, UserDAO userDAO) {
        this.hangarConfig = hangarConfig;
        this.userSignOnDAO = userSignOnDAO;
        this.restTemplate = restTemplate;
        this.userOauthTokenDAO = userOauthTokenDAO;
        this.tokenService = tokenService;
        this.userDAO = userDAO;
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

    public UserTable authenticate(String code, String nonce, String returnUrl) {
        String token = redeemCode(code, returnUrl);
        DecodedJWT decoded = JWT.decode(token);

        Traits traits = decoded.getClaim("traits").as(Traits.class);
        UserTable userTable = sync(traits);
        if (!isNonceValid(nonce)) {
            return null;
        }
        saveIdToken(token, traits.getUsername());
        return userTable;
    }

    public UserTable sync(Traits traits) {
        UserTable user = userDAO.getUserTable(traits.getUsername());
        if (user == null) {
            // TODO this breaks, we need to assign a user id here, it doesn't auto increment, maybe we want it to auto increment? also see Org Factory
            user = userDAO.create(traits.getUsername(), traits.getEmail(), traits.getName().getFirst() + " " + traits.getName().getLast(), "", traits.getLanguage(), List.of(), false);
        } else {
            user.setFullName(traits.getName().getFirst() + " " + traits.getName().getLast());
            user.setName(traits.getUsername());
            user.setEmail(traits.getEmail());
            user.setLanguage(traits.getLanguage());
            userDAO.update(user);
        }

        return user;
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

        ResponseEntity<TokenResponse> tokenResponse = restTemplate.exchange(hangarConfig.sso.getOauthUrl() + hangarConfig.sso.getTokenUrl(), HttpMethod.POST, entity, TokenResponse.class);

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
