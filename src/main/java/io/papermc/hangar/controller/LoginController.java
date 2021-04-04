package io.papermc.hangar.controller;

import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.api.auth.RefreshResponse;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.model.internal.sso.AuthUser;
import io.papermc.hangar.model.internal.sso.URLWithNonce;
import io.papermc.hangar.security.configs.SecurityConfig;
import io.papermc.hangar.service.AuthenticationService;
import io.papermc.hangar.service.TokenService;
import io.papermc.hangar.service.internal.auth.SSOService;
import io.papermc.hangar.service.internal.perms.roles.GlobalRoleService;
import io.papermc.hangar.service.internal.users.UserService;
import io.papermc.hangar.util.Routes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;

@Controller
public class LoginController extends HangarController {

    private final AuthenticationService authenticationService;
    private final SSOService ssoService;
    private final UserService userService;
    private final GlobalRoleService globalRoleService;
    private final TokenService tokenService;

    @Autowired
    public LoginController(AuthenticationService authenticationService, SSOService ssoService, UserService userService, GlobalRoleService globalRoleService, TokenService tokenService) {
        this.authenticationService = authenticationService;
        this.ssoService = ssoService;
        this.userService = userService;
        this.globalRoleService = globalRoleService;
        this.tokenService = tokenService;
    }

    @GetMapping(path = "/login", params = "returnUrl")
    public Object loginFromFrontend(@RequestParam(defaultValue = Routes.Paths.SHOW_HOME) String returnUrl) {
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

    @GetMapping(path = "/login", params = {"sso", "sig"})
    public ModelAndView loginFromAuth(@RequestParam String sso, @RequestParam String sig, @CookieValue String url, RedirectAttributes attributes) {
        AuthUser authUser = ssoService.authenticate(sso, sig);
        if (authUser == null) {
            throw new HangarApiException("nav.user.error.loginFailed");
        }

        UserTable user = userService.getOrCreate(authUser.getUserName(), authUser);
        globalRoleService.removeAllGlobalRoles(user.getId());
        authUser.getGlobalRoles().forEach(globalRole -> globalRoleService.addRole(globalRole.create(null, user.getId(), true)));
        String token = tokenService.createTokenForUser(user);
        return redirectBackOnSuccessfulLogin(url + "?token=" + token, user);
    }

    @GetMapping("/refresh")
    public ResponseEntity<RefreshResponse> refreshToken(@CookieValue(name = SecurityConfig.AUTH_NAME_REFRESH_COOKIE, required = false) String refreshToken) {
        return ResponseEntity.ok(tokenService.refreshToken(refreshToken));
    }

    @GetMapping("/invalidate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void invalidateRefreshToken(@CookieValue(name = SecurityConfig.AUTH_NAME_REFRESH_COOKIE) String refreshToken) {
        tokenService.invalidateToken(refreshToken);
    }

    // TODO needed?
    @PostMapping("/verify")
    public ModelAndView verify(@RequestParam String returnPath) {
        if (config.fakeUser.isEnabled()) {
            throw new HangarApiException("nav.user.error.fakeUserEnabled", "Verififcation");
        }
        return redirectToSso(ssoService.getVerifyUrl(config.getBaseUrl() + returnPath));
    }

    // TODO needed?
    @GetMapping("/logout")
    public ModelAndView logout(HttpSession session) {
        session.invalidate();
        return Routes.getRedirectToUrl(config.getAuthUrl() + "/accounts/logout/");
    }

    @GetMapping("/signup")
    public ModelAndView signUp(@RequestParam(defaultValue = "") String returnUrl) {
        if (config.fakeUser.isEnabled()) {
            throw new HangarApiException("nav.user.error.fakeUserEnabled", "Signup");
        }
        return redirectToSso(ssoService.getSignupUrl(returnUrl));
    }

    private ModelAndView redirectBackOnSuccessfulLogin(String url, UserTable user) {
        if (!url.startsWith("http")) {
            if (url.startsWith("/")) {
                url = config.getBaseUrl() + url;
            } else {
                url = config.getBaseUrl() + "/" + url;
            }
        }
        return Routes.getRedirectToUrl(url);
    }

    private ModelAndView redirectToSso(URLWithNonce urlWithNonce) {
        if (!config.sso.isEnabled()) {
            throw new HangarApiException("nav.user.error.loginDisabled");
        }
        ssoService.insert(urlWithNonce.getNonce());
        return Routes.getRedirectToUrl(urlWithNonce.getUrl());
    }
}
