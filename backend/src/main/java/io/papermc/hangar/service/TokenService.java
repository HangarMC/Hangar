package io.papermc.hangar.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.db.dao.internal.table.auth.ApiKeyDAO;
import io.papermc.hangar.db.dao.internal.table.auth.UserRefreshTokenDAO;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.common.Permission;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.model.db.auth.ApiKeyTable;
import io.papermc.hangar.model.db.auth.UserRefreshToken;
import io.papermc.hangar.security.authentication.HangarPrincipal;
import io.papermc.hangar.security.authentication.api.HangarApiPrincipal;
import io.papermc.hangar.security.configs.SecurityConfig;
import io.papermc.hangar.service.internal.users.UserService;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.UUID;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

@Service
public class TokenService extends HangarComponent {

    private final ApiKeyDAO apiKeyDAO;
    private final UserRefreshTokenDAO userRefreshTokenDAO;
    private final UserService userService;
    private final PermissionService permissionService;

    private JWTVerifier verifier;
    private Algorithm algo;

    @Autowired
    public TokenService(final ApiKeyDAO apiKeyDAO, final UserRefreshTokenDAO userRefreshTokenDAO, final UserService userService, final PermissionService permissionService) {
        this.apiKeyDAO = apiKeyDAO;
        this.userRefreshTokenDAO = userRefreshTokenDAO;
        this.userService = userService;
        this.permissionService = permissionService;
    }

    public DecodedJWT verify(final String token) {
        return this.getVerifier().verify(token);
    }

    public void issueRefreshToken(final UserTable userTable, final HttpServletResponse response) {
        final UserRefreshToken userRefreshToken = this.userRefreshTokenDAO.insert(new UserRefreshToken(userTable.getId(), UUID.randomUUID(), UUID.randomUUID()));
        this.addCookie(SecurityConfig.REFRESH_COOKIE_NAME, userRefreshToken.getToken().toString(), this.config.security.refreshTokenExpiry().toSeconds(), true, response);
    }

    private void addCookie(final String name, final String value, final long maxAge, final boolean httpOnly, final HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, ResponseCookie.from(name, value).path("/").secure(this.config.security.secure()).maxAge(maxAge).sameSite("Lax").httpOnly(httpOnly).build().toString());
    }

    public record RefreshResponse(String accessToken, UserTable userTable) {}

    public RefreshResponse refreshAccessToken(final String refreshToken) {
        if (refreshToken == null) {
            throw new HangarApiException(299, "No refresh token found");
        }
        final UUID uuid;
        try {
            uuid = UUID.fromString(refreshToken);
        } catch (final IllegalArgumentException ex) {
            throw new HangarApiException(HttpStatus.UNAUTHORIZED, "Invalid refresh token " + refreshToken);
        }
        UserRefreshToken userRefreshToken = this.userRefreshTokenDAO.getByToken(uuid);
        if (userRefreshToken == null) {
            throw new HangarApiException(HttpStatus.UNAUTHORIZED, "Unrecognized refresh token " + uuid);
        }
        if (userRefreshToken.getLastUpdated().isBefore(OffsetDateTime.now().minus(this.config.security.refreshTokenExpiry()))) {
            throw new HangarApiException(HttpStatus.UNAUTHORIZED, "Expired refresh token" + uuid);
        }
        final UserTable userTable = this.userService.getUserTable(userRefreshToken.getUserId());
        if (userTable == null) {
            throw new HangarApiException(HttpStatus.UNAUTHORIZED, "Unknown user");
        }
        // check if we gotta update the refresh token
        final Duration timeSinceLastUpdate = Duration.between(userRefreshToken.getLastUpdated(), OffsetDateTime.now());
        if (timeSinceLastUpdate.toDays() > 1) {
            userRefreshToken.setToken(UUID.randomUUID());
            userRefreshToken.setLastUpdated(OffsetDateTime.now());
            userRefreshToken = this.userRefreshTokenDAO.update(userRefreshToken);
        }
        // in any case, refreshing the cookie is good
        this.addCookie(SecurityConfig.REFRESH_COOKIE_NAME, userRefreshToken.getToken().toString(), this.config.security.refreshTokenExpiry().toSeconds(), true, this.response);
        // then issue a new access token
        return new RefreshResponse(this.newToken0(userTable), userTable);
    }

    public void invalidateToken(final String refreshToken) {
        if (refreshToken != null) {
            this.userRefreshTokenDAO.delete(UUID.fromString(refreshToken));
        }
        this.addCookie(SecurityConfig.REFRESH_COOKIE_NAME, null, 0, true, this.response);
        this.addCookie(SecurityConfig.AUTH_NAME, null, 0, false, this.response);
    }

    private String newToken0(final UserTable userTable) {
        final Permission globalPermissions = this.permissionService.getGlobalPermissions(userTable.getId());
        return this.expiring(userTable, globalPermissions, null);
    }

    public String expiring(final UserTable userTable, final Permission globalPermission, final @Nullable String apiKeyIdentifier) {
        return JWT.create()
            .withIssuer(this.config.security.tokenIssuer())
            .withExpiresAt(new Date(Instant.now().plus(this.config.security.tokenExpiry()).toEpochMilli()))
            .withSubject(userTable.getName())
            .withClaim("id", userTable.getId())
            .withClaim("permissions", globalPermission.toBinString())
            .withClaim("locked", userTable.isLocked())
            .withClaim("apiKeyIdentifier", apiKeyIdentifier)
            .sign(this.getAlgo());
    }

    public String simple(final String username) {
        return JWT.create()
            .withIssuer(this.config.security.tokenIssuer())
            .withExpiresAt(new Date(Instant.now().plus(this.config.security.tokenExpiry()).toEpochMilli()))
            .withSubject(username)
            .sign(this.getAlgo());
    }

    public HangarPrincipal parseHangarPrincipal(final DecodedJWT decodedJWT) {
        final String subject = decodedJWT.getSubject();
        final Long userId = decodedJWT.getClaim("id").asLong();
        final boolean locked = decodedJWT.getClaim("locked").asBoolean();
        final Permission globalPermission = Permission.fromBinString(decodedJWT.getClaim("permissions").asString());
        final String apiKeyIdentifier = decodedJWT.getClaim("apiKeyIdentifier").asString();
        if (subject == null || userId == null || globalPermission == null) {
            throw new BadCredentialsException("Malformed jwt");
        }
        if (apiKeyIdentifier != null) {
            final ApiKeyTable apiKeyTable = this.apiKeyDAO.findApiKey(userId, apiKeyIdentifier);
            if (apiKeyTable == null) {
                throw new BadCredentialsException("Invalid api key identifier");
            }
            return new HangarApiPrincipal(userId, subject, locked, globalPermission, apiKeyTable);
        } else {
            return new HangarPrincipal(userId, subject, locked, globalPermission);
        }
    }

    private JWTVerifier getVerifier() {
        if (this.verifier == null) {
            this.verifier = JWT.require(this.getAlgo())
                .acceptLeeway(10)
                .withIssuer(this.config.security.tokenIssuer())
                .build();
        }
        return this.verifier;
    }

    private Algorithm getAlgo() {
        if (this.algo == null) {
            this.algo = Algorithm.HMAC256(this.config.security.tokenSecret());
        }
        return this.algo;
    }
}
