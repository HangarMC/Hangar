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
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.UUID;

@Service
public class TokenService extends HangarComponent {

    private final ApiKeyDAO apiKeyDAO;
    private final UserRefreshTokenDAO userRefreshTokenDAO;
    private final UserService userService;
    private final PermissionService permissionService;

    private JWTVerifier verifier;
    private Algorithm algo;

    @Autowired
    public TokenService(ApiKeyDAO apiKeyDAO, UserRefreshTokenDAO userRefreshTokenDAO, UserService userService, PermissionService permissionService) {
        this.apiKeyDAO = apiKeyDAO;
        this.userRefreshTokenDAO = userRefreshTokenDAO;
        this.userService = userService;
        this.permissionService = permissionService;
    }

    public DecodedJWT verify(String token) {
        return getVerifier().verify(token);
    }

    public void issueRefreshAndAccessToken(UserTable userTable) {
        UserRefreshToken userRefreshToken = userRefreshTokenDAO.insert(new UserRefreshToken(userTable.getId(), UUID.randomUUID(), UUID.randomUUID()));
        addCookie(SecurityConfig.REFRESH_COOKIE_NAME, userRefreshToken.getToken().toString(), config.security.refreshTokenExpiry().toSeconds(), true);
        String accessToken = newToken0(userTable);
        // let the access token cookie be around for longer, so we can more nicely detect expired tokens via the response code
        addCookie(SecurityConfig.AUTH_NAME, accessToken, config.security.tokenExpiry().toSeconds() * 2, false);
    }

    private void addCookie(String name, String value, long maxAge, boolean httpOnly) {
        response.addHeader(HttpHeaders.SET_COOKIE, ResponseCookie.from(name, value).path("/").secure(config.security.secure()).maxAge(maxAge).sameSite("Lax").httpOnly(httpOnly).build().toString());
    }

    public void refreshAccessToken(String refreshToken) {
        if (refreshToken == null) {
            throw new HangarApiException(299, "No refresh token found");
        }
        UUID uuid;
        try {
            uuid = UUID.fromString(refreshToken);
        } catch (IllegalArgumentException ex) {
            throw new HangarApiException(HttpStatus.UNAUTHORIZED, "Invalid refresh token " + refreshToken);
        }
        UserRefreshToken userRefreshToken = userRefreshTokenDAO.getByToken(uuid);
        if (userRefreshToken == null) {
            throw new HangarApiException(HttpStatus.UNAUTHORIZED, "Unrecognized refresh token " + uuid);
        }
        if (userRefreshToken.getLastUpdated().isBefore(OffsetDateTime.now().minus(config.security.refreshTokenExpiry()))) {
            throw new HangarApiException(HttpStatus.UNAUTHORIZED, "Expired refresh token" + uuid);
        }
        UserTable userTable = userService.getUserTable(userRefreshToken.getUserId());
        if (userTable == null) {
            throw new HangarApiException(HttpStatus.UNAUTHORIZED, "Unknown user");
        }
        // we gotta update the refresh token
        userRefreshToken.setToken(UUID.randomUUID());
        userRefreshToken = userRefreshTokenDAO.update(userRefreshToken);
        addCookie(SecurityConfig.REFRESH_COOKIE_NAME, userRefreshToken.getToken().toString(), config.security.refreshTokenExpiry().toSeconds(), true);
        // then issue a new access token
        String accessToken = newToken0(userTable);
        addCookie(SecurityConfig.AUTH_NAME, accessToken, config.security.tokenExpiry().toSeconds(), false);
    }

    public void invalidateToken(String refreshToken) {
        if (refreshToken != null) {
            userRefreshTokenDAO.delete(UUID.fromString(refreshToken));
        }
        addCookie(SecurityConfig.REFRESH_COOKIE_NAME, null, 0, true);
        addCookie(SecurityConfig.AUTH_NAME, null, 0, false);
    }

    private String newToken0(UserTable userTable) {
        Permission globalPermissions = permissionService.getGlobalPermissions(userTable.getId());
        return expiring(userTable, globalPermissions, null);
    }

    public String expiring(UserTable userTable, Permission globalPermission, @Nullable String apiKeyIdentifier) {
        return JWT.create()
                .withIssuer(config.security.tokenIssuer())
                .withExpiresAt(new Date(Instant.now().plus(config.security.tokenExpiry()).toEpochMilli()))
                .withSubject(userTable.getName())
                .withClaim("id", userTable.getId())
                .withClaim("permissions", globalPermission.toBinString())
                .withClaim("locked", userTable.isLocked())
                .withClaim("apiKeyIdentifier", apiKeyIdentifier)
                .sign(getAlgo());
    }

    public String simple(String username) {
        return JWT.create()
                .withIssuer(config.security.tokenIssuer())
                .withExpiresAt(new Date(Instant.now().plus(config.security.tokenExpiry()).toEpochMilli()))
                .withSubject(username)
                .sign(getAlgo());
    }

    public HangarPrincipal parseHangarPrincipal(DecodedJWT decodedJWT) {
        String subject = decodedJWT.getSubject();
        Long userId = decodedJWT.getClaim("id").asLong();
        boolean locked = decodedJWT.getClaim("locked").asBoolean();
        Permission globalPermission = Permission.fromBinString(decodedJWT.getClaim("permissions").asString());
        String apiKeyIdentifier = decodedJWT.getClaim("apiKeyIdentifier").asString();
        if (subject == null || userId == null || globalPermission == null) {
            throw new BadCredentialsException("Malformed jwt");
        }
        if (apiKeyIdentifier != null) {
            ApiKeyTable apiKeyTable = apiKeyDAO.findApiKey(userId, apiKeyIdentifier);
            if (apiKeyTable == null) {
                throw new BadCredentialsException("Invalid api key identifier");
            }
            return new HangarApiPrincipal(userId, subject, locked, globalPermission, apiKeyTable);
        } else {
            return new HangarPrincipal(userId, subject, locked, globalPermission);
        }
    }

    private JWTVerifier getVerifier() {
        if (verifier == null) {
            verifier = JWT.require(getAlgo())
                    .acceptLeeway(10)
                    .withIssuer(config.security.tokenIssuer())
                    .build();
        }
        return verifier;
    }

    private Algorithm getAlgo() {
        if (algo == null) {
            algo = Algorithm.HMAC256(config.security.tokenSecret());
        }
        return algo;
    }
}
