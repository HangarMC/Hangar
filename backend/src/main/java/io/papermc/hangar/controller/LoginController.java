package io.papermc.hangar.controller;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.exceptions.WebHookException;
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
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpSession;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.view.RedirectView;
import java.util.Optional;

@Controller
@RateLimit(path = "login")
public class LoginController extends HangarComponent {

    private final AuthenticationService authenticationService;
    private final SSOService ssoService;
    private final TokenService tokenService;
    private final ValidationService validationService;

    @Autowired
    public LoginController(final AuthenticationService authenticationService, final SSOService ssoService, final TokenService tokenService, final ValidationService validationService) {
        this.authenticationService = authenticationService;
        this.ssoService = ssoService;
        this.tokenService = tokenService;
        this.validationService = validationService;
    }

    @GetMapping(path = "/login", params = "returnUrl")
    public RedirectView loginFromFrontend(@RequestParam(defaultValue = "/") final String returnUrl) {
        if (this.config.fakeUser.enabled()) {
            this.config.checkDev();

            final UserTable fakeUser = this.authenticationService.loginAsFakeUser();
            this.tokenService.issueRefreshToken(fakeUser);
            return this.addBaseAndRedirect(returnUrl);
        } else {
            this.response.addCookie(new Cookie("url", returnUrl));
            return this.redirectToSso(this.ssoService.getLoginUrl(this.config.getBaseUrl() + "/login"));
        }
    }

    @RateLimit(overdraft = 5, refillTokens = 5, refillSeconds = 10)
    @GetMapping(path = "/login", params = {"code", "state"})
    public RedirectView loginFromAuth(@RequestParam final String code, @RequestParam final String state, @CookieValue final String url) {
        final UserTable user = this.ssoService.authenticate(code, state, this.config.getBaseUrl() + "/login");
        if (user == null) {
            throw new HangarApiException("nav.user.error.loginFailed");
        }

        if (!this.validationService.isValidUsername(user.getName())) {
            throw new HangarApiException("nav.user.error.invalidUsername");
        }
        this.tokenService.issueRefreshToken(user);
        return this.addBaseAndRedirect(url);
    }

    @ResponseBody
    @GetMapping("/refresh")
    public String refreshAccessToken(@CookieValue(name = SecurityConfig.REFRESH_COOKIE_NAME, required = false) final String refreshToken) {
        return this.tokenService.refreshAccessToken(refreshToken).accessToken();
    }

    @GetMapping("/invalidate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void invalidateRefreshToken(@CookieValue(name = SecurityConfig.REFRESH_COOKIE_NAME, required = false) final String refreshToken) {
        this.tokenService.invalidateToken(refreshToken);
        final HttpSession session = this.request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    @ResponseBody
    @GetMapping(path = "/logout", params = "returnUrl")
    public String logout(@RequestParam(defaultValue = "/?loggedOut") final String returnUrl) {
        if (this.config.fakeUser.enabled()) {
            this.response.addCookie(new Cookie("url", returnUrl));
            return "/fake-logout";
        } else {
            this.response.addCookie(new Cookie("url", returnUrl));
            final Optional<HangarPrincipal> principal = this.getOptionalHangarPrincipal();
            if (principal.isPresent()) {
                return this.ssoService.getLogoutUrl(this.config.getBaseUrl() + "/handle-logout", principal.get()).getUrl();
            } else {
                this.tokenService.invalidateToken(null);
                return this.addBase(returnUrl);
            }
        }
    }

    @GetMapping(path = "/fake-logout")
    public RedirectView fakeLogout(@CookieValue(value = "url", defaultValue = "/logged-out") final String returnUrl, @CookieValue(name = SecurityConfig.REFRESH_COOKIE_NAME, required = false) final String refreshToken) {
        // invalidate refresh token
        if (refreshToken != null) {
            this.tokenService.invalidateToken(refreshToken);
        }
        // invalidate session
        final HttpSession session = this.request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return this.addBaseAndRedirect(returnUrl);
    }

    @GetMapping(path = "/handle-logout", params = "state")
    public RedirectView loggedOut(@RequestParam final String state, @CookieValue(value = "url", defaultValue = "/logged-out") final String returnUrl, @CookieValue(name = SecurityConfig.REFRESH_COOKIE_NAME, required = false) final String refreshToken) {
        final String username;
        try {
            // get username
            final DecodedJWT decodedJWT = this.tokenService.verify(state);
            username = decodedJWT.getSubject();
        } catch (final JWTVerificationException e) {
            throw new HangarApiException("nav.user.error.logoutFailed", e.getMessage());
        }

        // invalidate id token
        this.ssoService.logout(username);
        // invalidate refresh token
        if (refreshToken != null) {
            this.tokenService.invalidateToken(refreshToken);
        }
        // invalidate session
        final HttpSession session = this.request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return this.addBaseAndRedirect(returnUrl);
    }

    @GetMapping("/signup")
    public RedirectView signUp(@RequestParam(defaultValue = "") final String returnUrl) {
        if (this.config.fakeUser.enabled()) {
            throw new HangarApiException("nav.user.error.fakeUserEnabled", "Signup");
        }
        return new RedirectView(this.ssoService.getSignupUrl(returnUrl));
    }

    @PostMapping("/sync")
    @RateLimit(overdraft = 5, refillTokens = 2, refillSeconds = 10)
    public ResponseEntity sync(@RequestBody final @NotNull SsoSyncData body, @RequestHeader("X-Kratos-Hook-Api-Key") final String apiKey) {
        if (!apiKey.equals(this.config.sso.kratosApiKey())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        if (body.state().equals("active")) {
            this.logger.debug("Syncing {}'s new traits: {}", body.id(), body.traits());
            try {
                this.ssoService.sync(body.id(), body.traits());
            } catch (final WebHookException ex) {
                return ResponseEntity.badRequest().body(ex.getError());
            }
        } else {
            this.logger.debug("Not syncing since its not active! {}", body);
        }
        return ResponseEntity.accepted().build();
    }

    private RedirectView addBaseAndRedirect(final String url) {
        return new RedirectView(this.addBase(url));
    }

    private String addBase(String url) {
        if (!url.startsWith("http")) {
            if (url.startsWith("/")) {
                url = this.config.getBaseUrl() + url;
            } else {
                url = this.config.getBaseUrl() + "/" + url;
            }
        }
        return url;
    }

    private RedirectView redirectToSso(final URLWithNonce urlWithNonce) {
        if (!this.config.sso.enabled()) {
            throw new HangarApiException("nav.user.error.loginDisabled");
        }
        this.ssoService.insert(urlWithNonce.getNonce());
        return new RedirectView(urlWithNonce.getUrl());
    }
}
