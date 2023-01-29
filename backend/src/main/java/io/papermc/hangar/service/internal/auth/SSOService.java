package io.papermc.hangar.service.internal.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.papermc.hangar.config.hangar.HangarConfig;
import io.papermc.hangar.db.dao.internal.table.UserDAO;
import io.papermc.hangar.db.dao.internal.table.auth.UserOauthTokenDAO;
import io.papermc.hangar.db.dao.internal.table.auth.UserSignOnDAO;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.exceptions.WebHookException;
import io.papermc.hangar.model.api.UserNameChange;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.model.db.auth.UserOauthTokenTable;
import io.papermc.hangar.model.db.auth.UserSignOnTable;
import io.papermc.hangar.model.internal.sso.TokenResponse;
import io.papermc.hangar.model.internal.sso.Traits;
import io.papermc.hangar.model.internal.sso.URLWithNonce;
import io.papermc.hangar.security.authentication.HangarPrincipal;
import io.papermc.hangar.service.TokenService;
import io.papermc.hangar.service.internal.projects.ProjectService;
import java.math.BigInteger;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

@Service
public class SSOService {

    private static final Logger log = LoggerFactory.getLogger(SSOService.class);

    private final HangarConfig hangarConfig;
    private final UserSignOnDAO userSignOnDAO;
    private final RestTemplate restTemplate;
    private final UserOauthTokenDAO userOauthTokenDAO;
    private final TokenService tokenService;
    private final ProjectService projectService;
    private final UserDAO userDAO;

    @Autowired
    public SSOService(final HangarConfig hangarConfig, final UserSignOnDAO userSignOnDAO, final RestTemplate restTemplate, final UserOauthTokenDAO userOauthTokenDAO, final TokenService tokenService, final ProjectService projectService, final UserDAO userDAO) {
        this.hangarConfig = hangarConfig;
        this.userSignOnDAO = userSignOnDAO;
        this.restTemplate = restTemplate;
        this.userOauthTokenDAO = userOauthTokenDAO;
        this.tokenService = tokenService;
        this.projectService = projectService;
        this.userDAO = userDAO;
    }

    private boolean isNonceValid(final String nonce) {
        final UserSignOnTable userSignOn = this.userSignOnDAO.getByNonce(nonce);
        if (userSignOn == null) return false;
        final long millisSinceCreated = userSignOn.getCreatedAt().until(OffsetDateTime.now(), ChronoUnit.MILLIS);
        if (userSignOn.isCompleted() || millisSinceCreated > 600000) return false;
        this.userSignOnDAO.markCompleted(userSignOn.getId());
        return true;
    }

    public URLWithNonce getLoginUrl(final String returnUrl) {
        final String generatedNonce = this.nonce();
        final String url = UriComponentsBuilder.fromUriString(this.hangarConfig.sso.oauthUrl() + this.hangarConfig.sso.loginUrl())
            .queryParam("client_id", this.hangarConfig.sso.clientId())
            .queryParam("scope", "openid email profile")
            .queryParam("response_type", "code")
            .queryParam("redirect_uri", returnUrl)
            .queryParam("state", generatedNonce)
            .build().toUriString();

        return new URLWithNonce(url, generatedNonce);
    }

    private String idToken(final String username) {
        final UserOauthTokenTable token = this.userOauthTokenDAO.getByUsername(username);
        if (token == null) {
            log.warn("Can't log-out {} as there is no id token stored!", username);
            return null;
        }
        return token.getIdToken();
    }

    public URLWithNonce getLogoutUrl(final String returnUrl, final HangarPrincipal user) {
        final String idToken = this.idToken(user.getName());
        final String generatedNonce = this.nonce();
        final String url = UriComponentsBuilder.fromUriString(this.hangarConfig.sso.oauthUrl() + this.hangarConfig.sso.logoutUrl())
            .queryParam("post_logout_redirect_uri", returnUrl)
            .queryParam("id_token_hint", idToken)
            .queryParam("state", this.tokenService.simple(user.getName()))
            .build().toUriString();
        return new URLWithNonce(url, generatedNonce);
    }

    public String getSignupUrl(final String returnUrl) {
        // TODO figure out what we wanna do here
        return this.hangarConfig.sso.authUrl() + this.hangarConfig.sso.signupUrl();
    }

    private String nonce() {
        return new BigInteger(130, ThreadLocalRandom.current()).toString(32);
    }

    public UserTable authenticate(final String code, final String nonce, final String returnUrl) {
        final String token = this.redeemCode(code, returnUrl);
        final DecodedJWT decoded = JWT.decode(token);

        final UUID uuid = UUID.fromString(decoded.getSubject());
        final Traits traits = decoded.getClaim("traits").as(Traits.class);
        if (traits == null) {
            log.warn("Can't login {} as there are no traits!", uuid);
            return null;
        }
        final UserTable userTable = this.sync(uuid, traits);
        if (!this.isNonceValid(nonce)) {
            return null;
        }
        this.saveIdToken(token, traits.username());
        return userTable;
    }

    public UserTable sync(final UUID uuid, final Traits traits) {
        UserTable user = this.userDAO.getUserTable(uuid);
        boolean refreshHomeProjects = false;
        if (user == null) {
            user = this.userDAO.create(uuid, traits.username(), traits.email(), "", traits.language(), List.of(), false, traits.theme());
        } else {
            if (!user.getName().equals(traits.username())) {
                this.handleUsernameChange(user, traits.username());
                user.setName(traits.username());
                refreshHomeProjects = true; // must refresh this view when a username is changed
            }
            user.setEmail(traits.email());
            // only sync if set
            if (traits.language() != null) {
                user.setLanguage(traits.language());
            }
            if (traits.theme() != null) {
                user.setTheme(traits.theme());
            }
            this.userDAO.update(user);
            if (refreshHomeProjects) {
                this.projectService.refreshHomeProjects();
            }
        }

        return user;
    }

    private void handleUsernameChange(final UserTable user, final String newName) {
        // make sure a user with that name doesn't exist yet
        if (this.userDAO.getUserTable(newName) != null) {
            throw new HangarApiException("A user with that name already exists!");
        }
        // check that last change was long ago
        final List<UserNameChange> userNameHistory = this.userDAO.getUserNameHistory(user.getUuid());
        if (!userNameHistory.isEmpty()) {
            userNameHistory.sort(Comparator.comparing(UserNameChange::date).reversed());
            final OffsetDateTime nextChange = userNameHistory.get(0).date().plus(this.hangarConfig.user.nameChangeInterval(), ChronoUnit.DAYS);
            if (nextChange.isAfter(OffsetDateTime.now())) {
                throw WebHookException.of("You can't change your name that soon! You have to wait till " + nextChange.format(DateTimeFormatter.RFC_1123_DATE_TIME));
            }
        }
        // record the change into the db
        this.userDAO.recordNameChange(user.getUuid(), user.getName(), newName);
    }

    public String redeemCode(final String code, final String redirect) {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        final MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("code", code);
        map.add("grant_type", "authorization_code");
        map.add("client_id", this.hangarConfig.sso.clientId());
        map.add("redirect_uri", redirect);

        final HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);

        final ResponseEntity<TokenResponse> tokenResponse = this.restTemplate.exchange(this.hangarConfig.sso.backendOauthUrl() + this.hangarConfig.sso.tokenUrl(), HttpMethod.POST, entity, TokenResponse.class);

        return tokenResponse.getBody().getIdToken();
    }

    public UserSignOnTable insert(final String nonce) {
        return this.userSignOnDAO.insert(new UserSignOnTable(nonce));
    }

    public void saveIdToken(final String idToken, final String username) {
        this.userOauthTokenDAO.insert(new UserOauthTokenTable(username, idToken));
    }

    public void logout(final String username) {
        this.userOauthTokenDAO.remove(username);
    }
}
