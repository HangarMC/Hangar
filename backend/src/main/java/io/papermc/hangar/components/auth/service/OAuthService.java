package io.papermc.hangar.components.auth.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.components.auth.dao.UserCredentialDAO;
import io.papermc.hangar.components.auth.model.credential.CredentialType;
import io.papermc.hangar.components.auth.model.credential.OAuthCredential;
import io.papermc.hangar.components.auth.model.db.UserCredentialTable;
import io.papermc.hangar.components.auth.model.oauth.OAuthCodeRequest;
import io.papermc.hangar.components.auth.model.oauth.OAuthCodeResponse;
import io.papermc.hangar.components.auth.model.oauth.OAuthMode;
import io.papermc.hangar.components.auth.model.oauth.OAuthProvider;
import io.papermc.hangar.components.auth.model.oauth.OAuthUserDetails;
import io.papermc.hangar.db.customtypes.JSONB;
import io.papermc.hangar.db.dao.internal.table.UserDAO;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.db.UserTable;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.jdbi.v3.core.mapper.JoinRow;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class OAuthService extends HangarComponent {

    private final RestClient restClient;
    private final Algorithm algo;
    private final AuthService authService;
    private final UserDAO userDAO;
    private final VerificationService verificationService;
    private final UserCredentialDAO userCredentialDAO;

    private final Map<String, OAuthProvider> providers = new HashMap<>();

    public OAuthService(final RestClient restClient, final Algorithm algo, final AuthService authService, final UserDAO userDAO, final VerificationService verificationService, final UserCredentialDAO userCredentialDAO) {
        this.restClient = restClient;
        this.algo = algo;
        this.authService = authService;
        this.userDAO = userDAO;
        this.verificationService = verificationService;
        this.userCredentialDAO = userCredentialDAO;
        this.providers.put("github", new OAuthProvider("github", "26ba07861a06dda93f56", "d0cb6980a7c647b95cd30f8a2d6ac98b79cd67ac", new String[]{"user:email"}));
    }

    public String oauthState(final Long user, final OAuthMode mode, final String returnUrl) {
        return JWT.create()
            .withIssuer(this.config.security.tokenIssuer())
            .withExpiresAt(Instant.now().plus(10, ChronoUnit.MINUTES))
            .withSubject(String.valueOf(user))
            .withClaim("mode", mode.name())
            .withClaim("returnUrl", returnUrl)
            .sign(this.algo);
    }

    public String getLoginUrl(final String provider, final String state) {
        final OAuthProvider oAuthProvider = this.providers.get(provider);
        if (oAuthProvider == null) {
            throw new IllegalArgumentException("Unknown provider: " + provider);
        }

        // todo get from provider
        return UriComponentsBuilder.fromHttpUrl("https://github.com/login/oauth/authorize")
            .queryParam("client_id", oAuthProvider.clientId())
            .queryParam("redirect_uri", "http://localhost:3333/api/internal/oauth/" + oAuthProvider.name() + "/callback")
            .queryParam("scope", String.join(",", oAuthProvider.scopes()))
            .queryParam("state", state)
            .build().toUriString();
    }

    public String redeemCode(final String provider, final String code) {
        final OAuthProvider oAuthProvider = this.providers.get(provider);
        if (oAuthProvider == null) {
            throw new IllegalArgumentException("Unknown provider: " + provider);
        }

        // todo get from provider
        final OAuthCodeResponse response = this.restClient.post().uri("https://github.com/login/oauth/access_token")
            .header("Accept", "application/json")
            .body(new OAuthCodeRequest(oAuthProvider.clientId(), oAuthProvider.clientSecret(), code))
            .retrieve()
            .body(OAuthCodeResponse.class);

        if (response == null) {
            throw new IllegalStateException("Error in oauth code response");
        }
        return response.accessToken();
    }

    public OAuthUserDetails getUserDetails(final String provider, final String token) {
        final OAuthProvider oAuthProvider = this.providers.get(provider);
        if (oAuthProvider == null) {
            throw new IllegalArgumentException("Unknown provider: " + provider);
        }

        // todo get from provider
        // noinspection unchecked
        final Map<String, String> response = this.restClient.get().uri("https://api.github.com/user")
            .header("Authorization", "Bearer " + token)
            .retrieve()
            .body(Map.class);

        if (response == null) {
            throw new IllegalStateException("Error in oauth code response");
        }

        // todo get from provider
        // noinspection rawtypes
        final List response2 = this.restClient.get().uri("https://api.github.com/user/emails")
            .header("Authorization", "Bearer " + token)
            .retrieve()
            .body(List.class);

        // noinspection unchecked
        return new OAuthUserDetails(String.valueOf(response.get("id")), response.get("login"), (String) ((Map<String, Object>) response2.stream().filter(e -> ((Map<String, Boolean>) e).get("primary")).findFirst().orElseThrow()).get("email"));
    }

    public UserTable register(final String provider, final String id, final String username, final String email, final boolean tos, final boolean emailVerified) {
        this.authService.validateNewUser(username, email, tos);

        if (this.userCredentialDAO.getOAuthUser(provider, id) != null) {
            throw new HangarApiException("This " + provider + " account is already linked to a Hangar account");
        }

        final UserTable userTable = this.userDAO.create(UUID.randomUUID(), username, email, null, "en", List.of(), false, "light", false, new JSONB(Map.of()));

        if (!emailVerified) {
            this.verificationService.sendVerificationCode(userTable.getUserId(), userTable.getEmail(), userTable.getName());
        }

        return userTable;
    }

    public Map<String, OAuthProvider> getProviders() {
        return this.providers;
    }

    public UserTable login(final String provider, final String id, final String name) {
        final JoinRow result = this.userCredentialDAO.getOAuthUser(provider, id);
        if (result == null) {
            return null;
        }

        final UserCredentialTable userCredentialTable = result.get(UserCredentialTable.class);
        final UserTable userTable = result.get(UserTable.class);
        if (userCredentialTable == null || userTable == null) {
            return null;
        }

        final OAuthCredential oAuthCredential = userCredentialTable.getCredential().get(OAuthCredential.class);
        if (oAuthCredential == null) {
            return null;
        }

        if (!name.equals(oAuthCredential.name())) {
            this.userCredentialDAO.updateOAuth(userTable.getUserId(), new JSONB(new OAuthCredential(provider, id, name)), CredentialType.OAUTH, provider, id);
        }

        return userTable;
    }

    public String unlink(final String provider, final String id) {
        final OAuthProvider oAuthProvider = this.providers.get(provider);
        if (oAuthProvider == null) {
            throw new IllegalArgumentException("Unknown provider: " + provider);
        }

        this.userCredentialDAO.removeOAuth(this.getHangarPrincipal().getUserId(), CredentialType.OAUTH, provider, id);

        // TODO get from provider
        return "https://github.com/settings/connections/applications/" + oAuthProvider.clientId();
    }
}
