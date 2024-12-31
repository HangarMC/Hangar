package io.papermc.hangar.components.auth.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
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
import io.papermc.hangar.components.images.service.AvatarService;
import io.papermc.hangar.db.customtypes.JSONB;
import io.papermc.hangar.db.dao.internal.table.UserDAO;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.db.UserTable;
import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.jdbi.v3.core.mapper.JoinRow;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class OAuthService extends HangarComponent {

    private final RestClient restClient;
    private final Algorithm algo;
    private final JWT jwt;
    private final AuthService authService;
    private final UserDAO userDAO;
    private final VerificationService verificationService;
    private final UserCredentialDAO userCredentialDAO;
    private final CredentialsService credentialsService;
    private final AvatarService avatarService;

    private final Map<String, OAuthProvider> providers = new HashMap<>();

    public OAuthService(final RestClient restClient, final Algorithm algo, final JWT jwt, final AuthService authService, final UserDAO userDAO, final VerificationService verificationService, final UserCredentialDAO userCredentialDAO, final CredentialsService credentialsService, final AvatarService avatarService) {
        this.restClient = restClient;
        this.algo = algo;
        this.jwt = jwt;
        this.authService = authService;
        this.userDAO = userDAO;
        this.verificationService = verificationService;
        this.userCredentialDAO = userCredentialDAO;
        this.credentialsService = credentialsService;
        this.avatarService = avatarService;
    }

    @PostConstruct
    private void setup() {
        if (!this.config.security.oAuthEnabled()) {
            return;
        }
        this.config.security.oAuthProviders().forEach(this::setupProvider);
    }

    private void setupProvider(final OAuthProvider provider) {
        this.providers.put(provider.name(), provider);
        switch (provider.mode()) {
            case OIDC -> {
                if (provider.wellKnown() == null) {
                    throw new IllegalArgumentException("Provider " + provider.name() + " is missing well-known url");
                }
                // noinspection unchecked
                final Map<String, String> info = this.restClient.get().uri(provider.wellKnown())
                    .retrieve()
                    .body(Map.class);
                if (info == null) {
                    throw new IllegalStateException("Error in oauth well-known response for provider " + provider.name());
                }
                provider.authorizationEndpoint(info.get("authorization_endpoint"));
                provider.tokenEndpoint(info.get("token_endpoint"));
                provider.userInfoEndpoint(info.get("userinfo_endpoint"));
            }
            case GITHUB -> {
                provider.authorizationEndpoint("https://github.com/login/oauth/authorize");
                provider.tokenEndpoint("https://github.com/login/oauth/access_token");
                provider.userInfoEndpoint("https://api.github.com/user");
            }
            default -> throw new IllegalArgumentException("Unknown provider mode: " + provider.mode());
        }
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

    public String registerState(final OAuthUserDetails details, final String provider, final String returnUrl) {
        return JWT.create()
            .withIssuer(this.config.security.tokenIssuer())
            .withExpiresAt(Instant.now().plus(10, ChronoUnit.MINUTES))
            .withSubject(details.id())
            .withClaim("email", details.email())
            .withClaim("username", details.username())
            .withClaim("provider", provider)
            .withClaim("returnUrl", returnUrl)
            .sign(this.algo);
    }

    public String getLoginUrl(final String provider, final String state) {
        final OAuthProvider oAuthProvider = this.providers.get(provider);
        if (oAuthProvider == null) {
            throw new IllegalArgumentException("Unknown provider: " + provider);
        }

        final UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(oAuthProvider.authorizationEndpoint())
            .queryParam("client_id", oAuthProvider.clientId())
            .queryParam("redirect_uri", this.config.getBaseUrl() + "/api/internal/oauth/" + oAuthProvider.name() + "/callback")
            .queryParam("scope", String.join("+", oAuthProvider.scopes()))
            .queryParam("state", state);

        if (oAuthProvider.mode() == OAuthProvider.Mode.OIDC) {
            builder.queryParam("response_type", "code");
        }

        return builder
            .build().toUriString();
    }

    public OAuthCodeResponse redeemCode(final String provider, final String code) {
        final OAuthProvider oAuthProvider = this.providers.get(provider);
        if (oAuthProvider == null) {
            throw new IllegalArgumentException("Unknown provider: " + provider);
        }

        final OAuthCodeResponse response = switch (oAuthProvider.mode()) {
            case OIDC -> this.restClient.post().uri(oAuthProvider.tokenEndpoint())
                .header("Accept", "application/json")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .body(new LinkedMultiValueMap<>(Map.of(
                    "client_id", List.of(oAuthProvider.clientId()),
                    "client_secret", List.of(oAuthProvider.clientSecret()),
                    "code", List.of(code),
                    "grant_type", List.of("authorization_code"),
                    "redirect_uri", List.of(this.config.getBaseUrl() + "/api/internal/oauth/" + oAuthProvider.name() + "/callback")
                )))
                .retrieve()
                .body(OAuthCodeResponse.class);
            case GITHUB -> this.restClient.post().uri(oAuthProvider.tokenEndpoint())
                .header("Accept", "application/json")
                .body(new OAuthCodeRequest(oAuthProvider.clientId(), oAuthProvider.clientSecret(), code))
                .retrieve()
                .body(OAuthCodeResponse.class);
        };

        if (response == null) {
            throw new IllegalStateException("Error in oauth code response");
        }
        return response;
    }

    public OAuthUserDetails getUserDetails(final String provider, final OAuthCodeResponse codeResponse) {
        final OAuthProvider oAuthProvider = this.providers.get(provider);
        if (oAuthProvider == null) {
            throw new IllegalArgumentException("Unknown provider: " + provider);
        }

        return switch (oAuthProvider.mode()) {
            case OIDC -> {
                final DecodedJWT idToken = this.jwt.decodeJwt(codeResponse.idToken());
                String name = idToken.getClaim("preferred_username").asString();
                if (name == null) {
                    name = idToken.getClaim("name").asString();
                }
                yield new OAuthUserDetails(idToken.getSubject(), name, idToken.getClaim("email").asString());
            }
            case GITHUB -> {
                // noinspection unchecked
                final Map<String, String> response = this.restClient.get().uri(oAuthProvider.userInfoEndpoint())
                    .header("Authorization", "Bearer " + codeResponse.accessToken())
                    .retrieve()
                    .body(Map.class);

                if (response == null) {
                    throw new IllegalStateException("Error in oauth code response");
                }

                // noinspection rawtypes
                final List response2 = this.restClient.get().uri("https://api.github.com/user/emails")
                    .header("Authorization", "Bearer " + codeResponse.accessToken())
                    .retrieve()
                    .body(List.class);

                // noinspection unchecked
                yield new OAuthUserDetails(String.valueOf(response.get("id")),
                    response.get("login"),
                    (String) ((Map<String, Object>) response2.stream().filter(e -> ((Map<String, Boolean>) e).get("primary")).findFirst().orElseThrow()).get("email"));
            }
        };
    }

    public UserTable register(final String provider, final String id, final String username, final String email, final boolean tos, final boolean emailVerified) {
        this.authService.validateNewUser(username, email, tos);

        if (this.userCredentialDAO.getOAuthUser(provider, id, CredentialType.OAUTH) != null) {
            throw new HangarApiException("This " + provider + " account is already linked to a Hangar account");
        }

        final UserTable userTable = this.userDAO.create(UUID.randomUUID(), username, email, null, "en", List.of(), false, "light", emailVerified, this.avatarService.getDefaultAvatarUrl(), new JSONB(Map.of()));
        if (!emailVerified) {
            this.verificationService.sendVerificationCode(userTable.getUserId(), userTable.getEmail(), userTable.getName());
        }

        return userTable;
    }

    public Map<String, OAuthProvider> getProviders() {
        return this.providers;
    }

    public UserTable login(final String provider, final String id, final String name) {
        final JoinRow result = this.userCredentialDAO.getOAuthUser(provider, id, CredentialType.OAUTH);
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

        for (final OAuthCredential.OAuthConnection connection : oAuthCredential.connections()) {
            if (connection.provider().equals(provider) && connection.id().equals(id)) {
                if (!connection.name().equals(name)) {
                    oAuthCredential.connections().remove(connection);
                    oAuthCredential.connections().add(new OAuthCredential.OAuthConnection(provider, id, name));
                    this.credentialsService.updateCredential(userTable.getUserId(), oAuthCredential);
                } else {
                    break;
                }
            }
        }

        return userTable;
    }

    public String unlink(final String provider, final String id) {
        final OAuthProvider oAuthProvider = this.providers.get(provider);
        if (oAuthProvider == null) {
            throw new IllegalArgumentException("Unknown provider: " + provider);
        }

        final long userId = this.getHangarPrincipal().getUserId();
        final boolean hasPassword = this.credentialsService.getCredential(userId, CredentialType.PASSWORD) != null;
        final OAuthCredential oAuthCredential = this.getOAuthCredential(userId);
        if (!hasPassword && oAuthCredential.connections().size() == 1) {
            throw new HangarApiException("You can't remove your last oauth account without having a password set");
        }
        final boolean removedAny = oAuthCredential.connections().removeIf(c -> c.provider().equals(provider) && c.id().equals(id));
        if (!removedAny) {
            throw new HangarApiException("Unknown connection");
        }
        this.credentialsService.updateCredential(userId, oAuthCredential);

        return oAuthProvider.unlinkLink().replace(":id", oAuthProvider.clientId());
    }

    public void registerCredentials(final String provider, final long userId, final OAuthUserDetails userDetails) {
        final OAuthCredential.OAuthConnection connection = new OAuthCredential.OAuthConnection(provider, userDetails.id(), userDetails.username());
        final OAuthCredential oAuthCredential;
        try {
            // check if we already have connections
            oAuthCredential = this.getOAuthCredential(userId);
        } catch (final HangarApiException e) {
            // else just add
            this.credentialsService.registerCredential(userId, new OAuthCredential(List.of(connection)));
            return;
        }
        // check if this account already has that connection
        oAuthCredential.connections().stream().filter((c -> c.provider().equals(provider) && c.id().equals(userDetails.id()))).findFirst().ifPresent(c -> {
            throw new HangarApiException("This " + provider + " account is already linked to your Hangar account");
        });
        // check if another account already has that connection
        if (this.userCredentialDAO.getOAuthUser(provider, userDetails.id(), CredentialType.OAUTH) != null) {
            throw new HangarApiException("This " + provider + " account is already linked to another Hangar account");
        }
        // add
        oAuthCredential.connections().add(connection);
        this.credentialsService.updateCredential(userId, oAuthCredential);
    }

    private OAuthCredential getOAuthCredential(final long userId) {
        final UserCredentialTable oauth = this.credentialsService.getCredential(userId, CredentialType.OAUTH);
        if (oauth == null) {
            throw new HangarApiException("You don't have any oauth accounts linked");
        }
        final OAuthCredential oAuthCredential = oauth.getCredential().get(OAuthCredential.class);
        if (oAuthCredential == null) {
            throw new HangarApiException("You don't have any oauth accounts linked");
        }
        return oAuthCredential;
    }
}
