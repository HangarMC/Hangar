package io.papermc.hangar.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.papermc.hangar.controller.api.RefreshResponse;
import io.papermc.hangar.controller.extras.exceptions.HangarApiException;
import io.papermc.hangar.controller.utils.CookieUtils;
import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.internal.table.auth.UserRefreshTokenDAO;
import io.papermc.hangar.model.Permission;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.model.db.auth.UserRefreshToken;
import io.papermc.hangar.security.HangarPrincipal;
import io.papermc.hangar.security.configs.SecurityConfig;
import io.papermc.hangar.service.internal.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.UUID;

@Service
public class TokenService extends HangarService {

    private final UserRefreshTokenDAO userRefreshTokenDAO;
    private final UserService userService;
    private final PermissionService permissionService;

    private JWTVerifier verifier;
    private Algorithm algo;

    @Autowired
    public TokenService(HangarDao<UserRefreshTokenDAO> userRefreshTokenDAO, UserService userService, PermissionService permissionService) {
        this.userRefreshTokenDAO = userRefreshTokenDAO.get();
        this.userService = userService;
        this.permissionService = permissionService;
    }

    public DecodedJWT verify(String token) {
        return getVerifier().verify(token);
    }

    public String createTokenForUser(UserTable userTable) {
        UserRefreshToken userRefreshToken = userRefreshTokenDAO.insert(new UserRefreshToken(userTable.getId(), UUID.randomUUID(), UUID.randomUUID()));
        response.addCookie(CookieUtils.builder(SecurityConfig.AUTH_NAME_REFRESH_COOKIE, userRefreshToken.getToken().toString()).withComment("Refresh token for a JWT").setPath("/").setSecure(!hangarConfig.isUseWebpack()).setMaxAge((int) hangarConfig.security.getRefreshTokenExpiry().toSeconds()).build());
        return _newToken(userTable, userRefreshToken);
    }

    private String _newToken(UserTable userTable, UserRefreshToken userRefreshToken) {
        Permission globalPermissions = permissionService.getGlobalPermissions(userTable.getId());
        return expiring(userTable, globalPermissions);
    }

    public RefreshResponse refreshToken(String refreshToken) {
        if (refreshToken == null) {
            throw new HangarApiException(HttpStatus.UNAUTHORIZED, "No refresh token found");
        }
        UserRefreshToken userRefreshToken = userRefreshTokenDAO.getByToken(UUID.fromString(refreshToken));
        if (userRefreshToken == null) {
            throw new HangarApiException(HttpStatus.UNAUTHORIZED, "Unrecognized refresh token");
        }
        if (userRefreshToken.getLastUpdated().isBefore(OffsetDateTime.now().minus(hangarConfig.security.getRefreshTokenExpiry()))) {
            throw new HangarApiException(HttpStatus.UNAUTHORIZED, "Expired refresh token");
        }
        userRefreshToken.setToken(UUID.randomUUID());
        userRefreshToken = userRefreshTokenDAO.update(userRefreshToken);
        UserTable userTable = userService.getUserTable(userRefreshToken.getUserId());
        assert userTable != null;
        String token = _newToken(userTable, userRefreshToken);
        return new RefreshResponse(token, userRefreshToken.getToken().toString(), SecurityConfig.AUTH_NAME_REFRESH_COOKIE);
    }

    public void invalidateToken(String refreshToken) {
        userRefreshTokenDAO.delete(UUID.fromString(refreshToken));
    }

    public String expiring(UserTable userTable, Permission globalPermission) {
        return JWT.create()
                .withIssuer(hangarConfig.security.getTokenIssuer())
                .withExpiresAt(new Date(Instant.now().plus(hangarConfig.security.getTokenExpiry()).toEpochMilli()))
                .withSubject(userTable.getName())
                .withClaim("id", userTable.getId())
                .withClaim("permissions", globalPermission.toBinString())
                .sign(getAlgo());
    }

    public HangarPrincipal parseHangarPrincipal(DecodedJWT decodedJWT) {
        String subject = decodedJWT.getSubject();
        Long userId = decodedJWT.getClaim("id").asLong();
        Permission globalPermission = Permission.fromBinString(decodedJWT.getClaim("permissions").asString());
        if (subject == null || userId == null || globalPermission == null) {
            throw new BadCredentialsException("Malformed jwt");
        }
        return new HangarPrincipal(userId, subject, globalPermission);
    }

    private JWTVerifier getVerifier() {
        if (verifier == null) {
            verifier = JWT.require(getAlgo())
                    .withIssuer(hangarConfig.security.getTokenIssuer())
                    .build();
        }
        return verifier;
    }

    private Algorithm getAlgo() {
        if (algo == null) {
            algo = Algorithm.HMAC256(hangarConfig.security.getTokenSecret());
        }
        return algo;
    }
}
