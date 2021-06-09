package io.papermc.hangar.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.db.dao.internal.table.auth.ApiKeyDAO;
import io.papermc.hangar.db.dao.internal.table.auth.UserRefreshTokenDAO;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.api.auth.RefreshResponse;
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

    public String createTokenForUser(UserTable userTable) {
        UserRefreshToken userRefreshToken = userRefreshTokenDAO.insert(new UserRefreshToken(userTable.getId(), UUID.randomUUID(), UUID.randomUUID()));
        response.addHeader(HttpHeaders.SET_COOKIE, ResponseCookie.from(SecurityConfig.AUTH_NAME_REFRESH_COOKIE, userRefreshToken.getToken().toString()).path("/").secure(config.security.isSecure()).maxAge(config.security.getRefreshTokenExpiry().toSeconds()).sameSite("Strict").build().toString());
        return newToken0(userTable);
    }

    private String newToken0(UserTable userTable) {
        Permission globalPermissions = permissionService.getGlobalPermissions(userTable.getId());
        return expiring(userTable, globalPermissions, null);
    }

    public RefreshResponse refreshToken(String refreshToken) {
        if (refreshToken == null) {
            throw new HangarApiException(HttpStatus.UNAUTHORIZED, "No refresh token found");
        }
        UserRefreshToken userRefreshToken = userRefreshTokenDAO.getByToken(UUID.fromString(refreshToken));
        if (userRefreshToken == null) {
            throw new HangarApiException(HttpStatus.UNAUTHORIZED, "Unrecognized refresh token");
        }
        if (userRefreshToken.getLastUpdated().isBefore(OffsetDateTime.now().minus(config.security.getRefreshTokenExpiry()))) {
            throw new HangarApiException(HttpStatus.UNAUTHORIZED, "Expired refresh token");
        }
        userRefreshToken.setToken(UUID.randomUUID());
        userRefreshToken = userRefreshTokenDAO.update(userRefreshToken);
        UserTable userTable = userService.getUserTable(userRefreshToken.getUserId());
        assert userTable != null;
        String token = newToken0(userTable);
        return new RefreshResponse(token, userRefreshToken.getToken().toString(), config.security.getRefreshTokenExpiry().toSeconds(), SecurityConfig.AUTH_NAME_REFRESH_COOKIE);
    }

    public void invalidateToken(String refreshToken) {
        userRefreshTokenDAO.delete(UUID.fromString(refreshToken));
    }

    public String expiring(UserTable userTable, Permission globalPermission, @Nullable String apiKeyIdentifier) {
        return JWT.create()
                .withIssuer(config.security.getTokenIssuer())
                .withExpiresAt(new Date(Instant.now().plus(config.security.getTokenExpiry()).toEpochMilli()))
                .withSubject(userTable.getName())
                .withClaim("id", userTable.getId())
                .withClaim("permissions", globalPermission.toBinString())
                .withClaim("locked", userTable.isLocked())
                .withClaim("apiKeyIdentifier", apiKeyIdentifier)
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
                    .withIssuer(config.security.getTokenIssuer())
                    .build();
        }
        return verifier;
    }

    private Algorithm getAlgo() {
        if (algo == null) {
            algo = Algorithm.HMAC256(config.security.getTokenSecret());
        }
        return algo;
    }
}
