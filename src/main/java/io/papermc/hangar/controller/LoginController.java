package io.papermc.hangar.controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.api.auth.RefreshResponse;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.model.internal.sso.SsoSyncData;
import io.papermc.hangar.model.internal.sso.URLWithNonce;
import io.papermc.hangar.security.annotations.LoggedIn;
import io.papermc.hangar.security.configs.SecurityConfig;
import io.papermc.hangar.service.AuthenticationService;
import io.papermc.hangar.service.TokenService;
import io.papermc.hangar.service.ValidationService;
import io.papermc.hangar.service.internal.auth.SSOService;

@Controller
public class LoginController extends HangarComponent {

    private final AuthenticationService authenticationService;
    private final SSOService ssoService;
    private final TokenService tokenService;
    private final ValidationService validationService;

    @Autowired
    public LoginController(AuthenticationService authenticationService, SSOService ssoService, TokenService tokenService, ValidationService validationService) {
        this.authenticationService = authenticationService;
        this.ssoService = ssoService;
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
        UserTable user = ssoService.authenticate(code, state, config.getBaseUrl() + "/login");
        if (user == null) {
            throw new HangarApiException("nav.user.error.loginFailed");
        }

        if (!validationService.isValidUsername(user.getName())) {
            throw new HangarApiException("nav.user.error.invalidUsername");
        }
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

    @PostMapping("/sync")
    public void sync(@NotNull @RequestBody SsoSyncData body, @RequestHeader("X-Kratos-Hook-Api-Key") String apiKey) {
        if (!apiKey.equals("hookapikey-changeme")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        if (body.state().equals("active")) {
            logger.debug("Syncing {}", body.traits());
            ssoService.sync(body.traits());
        } else {
            logger.debug("Not syncing since its not active! {}", body);
        }
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
