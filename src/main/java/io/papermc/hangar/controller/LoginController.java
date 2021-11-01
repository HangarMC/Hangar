package io.papermc.hangar.controller;

import com.auth0.jwt.interfaces.DecodedJWT;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.db.dao.internal.table.auth.UserOauthTokenDAO;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.api.auth.RefreshResponse;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.model.internal.sso.AuthUser;
import io.papermc.hangar.model.internal.sso.URLWithNonce;
import io.papermc.hangar.security.annotations.LoggedIn;
import io.papermc.hangar.security.configs.SecurityConfig;
import io.papermc.hangar.service.AuthenticationService;
import io.papermc.hangar.service.TokenService;
import io.papermc.hangar.service.ValidationService;
import io.papermc.hangar.service.internal.auth.SSOService;
import io.papermc.hangar.service.internal.perms.roles.GlobalRoleService;
import io.papermc.hangar.service.internal.users.UserService;

@Controller
public class LoginController extends HangarComponent {

    private final AuthenticationService authenticationService;
    private final SSOService ssoService;
    private final UserService userService;
    private final GlobalRoleService globalRoleService;
    private final TokenService tokenService;
    private final ValidationService validationService;

    @Autowired
    public LoginController(AuthenticationService authenticationService, SSOService ssoService, UserService userService, GlobalRoleService globalRoleService, TokenService tokenService, ValidationService validationService) {
        this.authenticationService = authenticationService;
        this.ssoService = ssoService;
        this.userService = userService;
        this.globalRoleService = globalRoleService;
        this.tokenService = tokenService;
        this.validationService = validationService;
    }

    @GetMapping(path = "/login", params = "returnUrl")
    public RedirectView loginFromFrontend(@RequestParam(defaultValue = "/") String returnUrl) {
        if (config.fakeUser.isEnabled()) {
            config.checkDev();

            UserTable fakeUser = authenticationService.loginAsFakeUser();
            tokenService.createTokenForUser(fakeUser);
            return new RedirectView(returnUrl);
        } else {
            response.addCookie(new Cookie("url", returnUrl));
            return redirectToSso(ssoService.getLoginUrl(config.getBaseUrl() + "/login"));
        }
    }

    @GetMapping(path = "/login", params = {"code", "state"})
    public RedirectView loginFromAuth(@RequestParam String code, @RequestParam String state, @CookieValue String url) {
        AuthUser authUser = ssoService.authenticate(code, state, config.getBaseUrl() + "/login");
        if (authUser == null) {
            throw new HangarApiException("nav.user.error.loginFailed");
        }

        if (!validationService.isValidUsername(authUser.getUserName())) {
            throw new HangarApiException("nav.user.error.invalidUsername");
        }

        UserTable user = userService.getOrCreate(authUser.getUserName(), authUser);
        globalRoleService.removeAllGlobalRoles(user.getId());
        authUser.getGlobalRoles().forEach(globalRole -> globalRoleService.addRole(globalRole.create(null, user.getId(), true)));
        tokenService.createTokenForUser(user);
        return addBaseAndRedirect(url);
    }

    @GetMapping("/refresh")
    public ResponseEntity<RefreshResponse> refreshToken(@CookieValue(name = SecurityConfig.AUTH_NAME_REFRESH_COOKIE, required = false) String refreshToken) {
        return ResponseEntity.ok(tokenService.refreshToken(refreshToken));
    }

    @GetMapping("/invalidate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void invalidateRefreshToken(@CookieValue(name = SecurityConfig.AUTH_NAME_REFRESH_COOKIE) String refreshToken) {
        tokenService.invalidateToken(refreshToken);
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    @LoggedIn
    @GetMapping(path = "/logout", params = "returnUrl")
    public RedirectView logout(@RequestParam(defaultValue = "/logged-out") String returnUrl) {
        if (config.fakeUser.isEnabled()) {
            response.addCookie(new Cookie("url", returnUrl));
            return new RedirectView("/handle-logout");
        } else {
            response.addCookie(new Cookie("url", returnUrl));
            return redirectToSso(ssoService.getLogoutUrl(config.getBaseUrl() + "/handle-logout", getHangarPrincipal()));
        }
    }

    @GetMapping(path = "/handle-logout", params = "state")
    public RedirectView loggedOut(@RequestParam String state, @CookieValue(value = "url", defaultValue = "/logged-out") String returnUrl, @CookieValue(name = SecurityConfig.AUTH_NAME_REFRESH_COOKIE, required = false) String refreshToken) {
        // get username
        DecodedJWT decodedJWT = tokenService.verify(state);
        String username = decodedJWT.getSubject();
        // invalidate id token
        ssoService.logout(username);
        // invalidate refresh token
        if (refreshToken != null) {
            tokenService.invalidateToken(refreshToken);
        }
        // invalidate session
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return addBaseAndRedirect(returnUrl);
    }

    @GetMapping("/signup")
    public RedirectView signUp(@RequestParam(defaultValue = "") String returnUrl) {
        if (config.fakeUser.isEnabled()) {
            throw new HangarApiException("nav.user.error.fakeUserEnabled", "Signup");
        }
        return new RedirectView(ssoService.getSignupUrl(returnUrl));
    }

    private RedirectView addBaseAndRedirect(String url) {
        if (!url.startsWith("http")) {
            if (url.startsWith("/")) {
                url = config.getBaseUrl() + url;
            } else {
                url = config.getBaseUrl() + "/" + url;
            }
        }
        return new RedirectView(url);
    }

    private RedirectView redirectToSso(URLWithNonce urlWithNonce) {
        if (!config.sso.isEnabled()) {
            throw new HangarApiException("nav.user.error.loginDisabled");
        }
        ssoService.insert(urlWithNonce.getNonce());
        return new RedirectView(urlWithNonce.getUrl());
    }
}
