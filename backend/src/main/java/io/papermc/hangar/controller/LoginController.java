package io.papermc.hangar.controller;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

import java.util.Optional;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.model.internal.sso.SsoSyncData;
import io.papermc.hangar.model.internal.sso.URLWithNonce;
import io.papermc.hangar.security.annotations.ratelimit.RateLimit;
import io.papermc.hangar.security.authentication.HangarPrincipal;
import io.papermc.hangar.security.configs.SecurityConfig;
import io.papermc.hangar.service.AuthenticationService;
import io.papermc.hangar.service.TokenService;
import io.papermc.hangar.service.ValidationService;
import io.papermc.hangar.service.internal.auth.SSOService;

@Controller
@RateLimit(path = "login")
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
            tokenService.issueRefreshAndAccessToken(fakeUser);
            return new RedirectView(returnUrl);
        } else {
            response.addCookie(new Cookie("url", returnUrl));
            return redirectToSso(ssoService.getLoginUrl(config.getBaseUrl() + "/login"));
        }
    }

    @RateLimit(overdraft = 5, refillTokens = 5, refillSeconds = 10)
    @GetMapping(path = "/login", params = {"code", "state"})
    public RedirectView loginFromAuth(@RequestParam String code, @RequestParam String state, @CookieValue String url) {
        UserTable user = ssoService.authenticate(code, state, config.getBaseUrl() + "/login");
        if (user == null) {
            throw new HangarApiException("nav.user.error.loginFailed");
        }

        if (!validationService.isValidUsername(user.getName())) {
            throw new HangarApiException("nav.user.error.invalidUsername");
        }
        tokenService.issueRefreshAndAccessToken(user);
        return addBaseAndRedirect(url);
    }

    @GetMapping("/refresh")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void refreshAccessToken(@CookieValue(name = SecurityConfig.REFRESH_COOKIE_NAME, required = false) String refreshToken) {
        tokenService.refreshAccessToken(refreshToken);
    }

    @GetMapping("/invalidate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void invalidateRefreshToken(@CookieValue(name = SecurityConfig.REFRESH_COOKIE_NAME, required = false) String refreshToken) {
        tokenService.invalidateToken(refreshToken);
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    @GetMapping(path = "/logout", params = "returnUrl")
    public RedirectView logout(@RequestParam(defaultValue = "/logged-out") String returnUrl) {
        if (config.fakeUser.isEnabled()) {
            response.addCookie(new Cookie("url", returnUrl));
            return new RedirectView("/fake-logout");
        } else {
            response.addCookie(new Cookie("url", returnUrl));
            Optional<HangarPrincipal> principal = getOptionalHangarPrincipal();
            if (principal.isPresent()) {
                return redirectToSso(ssoService.getLogoutUrl(config.getBaseUrl() + "/handle-logout", principal.get()));
            } else {
                tokenService.invalidateToken(null);
                return new RedirectView(returnUrl);
            }
        }
    }

    @GetMapping(path = "/fake-logout")
    public RedirectView fakeLogout(@CookieValue(value = "url", defaultValue = "/logged-out") String returnUrl, @CookieValue(name = SecurityConfig.REFRESH_COOKIE_NAME, required = false) String refreshToken) {
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

    @GetMapping(path = "/handle-logout", params = "state")
    public RedirectView loggedOut(@RequestParam String state, @CookieValue(value = "url", defaultValue = "/logged-out") String returnUrl, @CookieValue(name = SecurityConfig.REFRESH_COOKIE_NAME, required = false) String refreshToken) {
        String username;
        try {
            // get username
            DecodedJWT decodedJWT = tokenService.verify(state);
            username = decodedJWT.getSubject();
        } catch (JWTVerificationException e) {
            throw new HangarApiException("nav.user.error.logoutFailed", e.getMessage());
        }

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
    @RateLimit(overdraft = 5, refillTokens = 2, refillSeconds = 10)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void sync(@NotNull @RequestBody SsoSyncData body, @RequestHeader("X-Kratos-Hook-Api-Key") String apiKey) {
        if (!apiKey.equals("hookapikey-changeme")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        if (body.state().equals("active")) {
            logger.debug("Syncing {}'s new traits: {}", body.id(), body.traits());
            ssoService.sync(body.id(), body.traits());
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
