package io.papermc.hangar.controller.internal;

import io.papermc.hangar.controller.HangarController;
import io.papermc.hangar.controller.api.RefreshResponse;
import io.papermc.hangar.exceptions.HangarException;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.model.internal.sso.AuthUser;
import io.papermc.hangar.model.internal.sso.URLWithNonce;
import io.papermc.hangar.security.configs.SecurityConfig;
import io.papermc.hangar.service.AuthenticationService;
import io.papermc.hangar.service.TokenService;
import io.papermc.hangar.service.internal.SSOService;
import io.papermc.hangar.service.internal.UserService;
import io.papermc.hangar.service.internal.roles.GlobalRoleService;
import io.papermc.hangar.util.AlertUtil;
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
    public Object loginFromFrontend(@RequestParam(defaultValue = Routes.Paths.SHOW_HOME) String returnUrl, RedirectAttributes attributes) {
        if (hangarConfig.fakeUser.isEnabled()) {
            hangarConfig.checkDev();

            UserTable fakeUser = authenticationService.loginAsFakeUser();
            tokenService.createTokenForUser(fakeUser);
            return new RedirectView(returnUrl);
        } else {
            try {
                response.addCookie(new Cookie("url", returnUrl));
                return redirectToSso(ssoService.getLoginUrl(hangarConfig.getBaseUrl() + "/login"), attributes);
            } catch (HangarException e) {
                AlertUtil.showAlert(attributes, AlertUtil.AlertType.ERROR, e.getMessageKey(), e.getArgs());
                return Routes.SHOW_HOME.getRedirect();
            }
        }
    }

    @GetMapping(path = "/login", params = {"sso", "sig"})
    public ModelAndView loginFromAuth(@RequestParam String sso, @RequestParam String sig, @CookieValue String url, RedirectAttributes attributes) {
        AuthUser authUser = ssoService.authenticate(sso, sig);
        if (authUser == null) {
            AlertUtil.showAlert(attributes, AlertUtil.AlertType.ERROR, "error.loginFailed");
            return Routes.SHOW_HOME.getRedirect();
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

//    @GetMapping("/login")
//    public ModelAndView login(@RequestParam(defaultValue = "") String sso, @RequestParam(defaultValue = "") String sig, @RequestParam(defaultValue = "") String returnUrl, @CookieValue(value = "url", required = false) String redirectUrl, RedirectAttributes attributes) {
//        if (hangarConfig.fakeUser.isEnabled()) {
//            hangarConfig.checkDebug();
//
//            UserTable fakeUser = authenticationService.loginAsFakeUser();
//
//            return redirectBackOnSuccessfulLogin(returnUrl, fakeUser);
//        } else if (sso.isEmpty()) {
//            String returnPath = returnUrl.isBlank() ? request.getRequestURI() : returnUrl;
//            try {
//                response.addCookie(new Cookie("url", returnPath));
//                return redirectToSso(ssoService.getLoginUrl(hangarConfig.getBaseUrl() + "/login"), attributes);
//            } catch (HangarException e) {
//                AlertUtil.showAlert(attributes, AlertUtil.AlertType.ERROR, e.getMessageKey(), e.getArgs());
//                return Routes.SHOW_HOME.getRedirect();
//            }
//
//        } else {
//            AuthUser authUser = ssoService.authenticate(sso, sig);
//            if (authUser == null) {
//                AlertUtil.showAlert(attributes, AlertUtil.AlertType.ERROR, "error.loginFailed");
//                return Routes.SHOW_HOME.getRedirect();
//            }
//
//            UserTable user = userService.getOrCreate(authUser.getUserName(), authUser);
//            roleService.removeAllGlobalRoles(user.getId());
//            authUser.getGlobalRoles().forEach(globalRole -> roleService.addRole(globalRole.create(null, user.getId(), true)));
//            authenticationService.setAuthenticatedUser(user);
//
//            String redirectPath = redirectUrl != null ? redirectUrl : Routes.getRouteUrlOf("showHome");
//            return redirectBackOnSuccessfulLogin(redirectPath, user);
//        }
//    }

    @PostMapping("/verify")
    public ModelAndView verify(@RequestParam String returnPath, RedirectAttributes attributes) {
        try {
            return redirectToSso(ssoService.getVerifyUrl(hangarConfig.getBaseUrl() + returnPath), attributes);
        } catch (HangarException e) {
            AlertUtil.showAlert(attributes, AlertUtil.AlertType.ERROR, e.getMessageKey(), e.getArgs());
            return Routes.SHOW_HOME.getRedirect();
        }
    }

    @GetMapping("/logout")
    public ModelAndView logout(HttpSession session) {
        // TODO flash
        session.invalidate();
        return Routes.getRedirectToUrl(hangarConfig.getAuthUrl() + "/accounts/logout/");
    }

    @GetMapping("/signup")
    public ModelAndView signUp(@RequestParam(defaultValue = "") String returnUrl, RedirectAttributes attributes) {
        try {
            return redirectToSso(ssoService.getSignupUrl(returnUrl), attributes);
        } catch (HangarException e) {
            AlertUtil.showAlert(attributes, AlertUtil.AlertType.ERROR, e.getMessageKey(), e.getArgs());
            return Routes.SHOW_HOME.getRedirect();
        }
    }

    private ModelAndView redirectBackOnSuccessfulLogin(String url, UserTable user) {
        if (!url.startsWith("http")) {
            if (url.startsWith("/")) {
                url = hangarConfig.getBaseUrl() + url;
            } else {
                url = hangarConfig.getBaseUrl() + "/" + url;
            }
        }
//        response.addCookie(CookieUtils.builder(HangarAuthenticationFilter.AUTH_NAME, tokenService.expiring()));
        return Routes.getRedirectToUrl(url);
    }

    private ModelAndView redirectToSso(URLWithNonce urlWithNonce, RedirectAttributes attributes) {
        if (!hangarConfig.sso.isEnabled()) {
            AlertUtil.showAlert(attributes, AlertUtil.AlertType.ERROR, "error.noLogin");
            return Routes.SHOW_HOME.getRedirect();
        }
        ssoService.insert(urlWithNonce.getNonce());
        return Routes.getRedirectToUrl(urlWithNonce.getUrl());
    }
}
