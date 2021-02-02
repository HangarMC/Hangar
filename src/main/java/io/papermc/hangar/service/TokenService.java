package io.papermc.hangar.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.papermc.hangar.controller.extras.exceptions.HangarApiException;
import io.papermc.hangar.controller.utils.CookieUtils;
import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.internal.table.auth.UserRefreshTokenDAO;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.model.db.auth.UserRefreshToken;
import io.papermc.hangar.model.roles.GlobalRole;
import io.papermc.hangar.security.internal.HangarAuthenticationFilter;
import io.papermc.hangar.service.internal.RoleService;
import io.papermc.hangar.service.internal.UserService;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TokenService extends HangarService {

    private final UserRefreshTokenDAO userRefreshTokenDAO;
    private final UserService userService;
    private final RoleService roleService;

    private JWTVerifier verifier;
    private Algorithm algo;

    @Autowired
    public TokenService(HangarDao<UserRefreshTokenDAO> userRefreshTokenDAO, UserService userService, RoleService roleService) {
        this.userRefreshTokenDAO = userRefreshTokenDAO.get();
        this.userService = userService;
        this.roleService = roleService;
    }

    public DecodedJWT verify(String token) {
        return getVerifier().verify(token);
    }

    public String createTokenForUser(UserTable userTable, HttpServletResponse response) {
        List<String> roles = roleService.getGlobalRoles(userTable.getId()).stream().map(GlobalRole::getValue).collect(Collectors.toList());
        UserRefreshToken userRefreshToken = userRefreshTokenDAO.insert(new UserRefreshToken(userTable.getId(), RandomStringUtils.randomAlphanumeric(128)));
        response.addCookie(CookieUtils.builder(HangarAuthenticationFilter.AUTH_NAME + "_REFRESH", userRefreshToken.getToken()).setHttpOnly(true).setPath("/").setMaxAge(60 * 60 * 24 * 7).build());
        return expiring(userTable.getName(), roles);
    }

    public String refreshToken(String refreshToken, HttpServletResponse response) {
        if (refreshToken == null) {
            throw new HangarApiException(HttpStatus.UNAUTHORIZED);
        }
        UserRefreshToken userRefreshToken = userRefreshTokenDAO.getByToken(refreshToken);
        if (userRefreshToken == null) {
            throw new HangarApiException(HttpStatus.UNAUTHORIZED);
        }
        UserTable userTable = userService.getUserTable(userRefreshToken.getUserId());
        assert userTable != null;
        return createTokenForUser(userTable, response);
    }

    public String expiring(String name, List<String> roles) {
        return JWT.create()
                .withIssuer(hangarConfig.security.getTokenIssuer())
                .withExpiresAt(new Date(Instant.now().plusSeconds(hangarConfig.security.getTokenExpiry()).toEpochMilli()))
                .withSubject(name)
                .withClaim("roles", roles)
                .sign(getAlgo());
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
