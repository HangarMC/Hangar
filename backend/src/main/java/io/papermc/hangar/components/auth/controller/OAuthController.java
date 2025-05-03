package io.papermc.hangar.components.auth.controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.components.auth.model.dto.OAuthSignupForm;
import io.papermc.hangar.components.auth.model.dto.OAuthSignupResponse;
import io.papermc.hangar.components.auth.model.oauth.OAuthCodeResponse;
import io.papermc.hangar.components.auth.model.oauth.OAuthMode;
import io.papermc.hangar.components.auth.model.oauth.OAuthUserDetails;
import io.papermc.hangar.components.auth.service.AuthService;
import io.papermc.hangar.components.auth.service.OAuthService;
import io.papermc.hangar.components.auth.service.TokenService;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.exceptions.handlers.ErrorRedirect;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.security.annotations.aal.RequireAal;
import io.papermc.hangar.security.annotations.ratelimit.RateLimit;
import java.io.IOException;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RateLimit(path = "auth")
@RequestMapping(path = "/api/internal/oauth", produces = MediaType.APPLICATION_JSON_VALUE)
public class OAuthController extends HangarComponent {

    private final OAuthService oAuthService;
    private final TokenService tokenService;
    private final AuthService authService;

    public OAuthController(final OAuthService oAuthService, final TokenService tokenService, final AuthService authService) {
        this.oAuthService = oAuthService;
        this.tokenService = tokenService;
        this.authService = authService;
    }

    @ErrorRedirect
    @GetMapping("/{provider}/login")
    public String login(@PathVariable final String provider, @RequestParam final OAuthMode mode, @RequestParam(required = false) final String returnUrl) throws IOException {
        if (!returnUrl.startsWith("/") || returnUrl.contains("..")) {
            throw new HangarApiException("Invalid return url");
        }

        final String state = this.oAuthService.oauthState(this.getHangarUserId(), mode, returnUrl);
        final String loginUrl = this.oAuthService.getLoginUrl(provider, state);
        if (mode == OAuthMode.SETTINGS) {
            return loginUrl;
        } else {
            this.redirect(loginUrl);
            return null;
        }
    }

    @ErrorRedirect
    @GetMapping("/{provider}/callback")
    public void callback(@PathVariable final String provider,
                         @RequestParam(required = false) final String code,
                         @RequestParam final String state,
                         @RequestParam(required = false) final String error,
                         @RequestParam(required = false, name = "error_description") final String errorDescription) throws IOException {
        final DecodedJWT decodedState = this.tokenService.verify(state);
        final OAuthMode mode = decodedState.getClaim("mode").as(OAuthMode.class);
        final String returnUrl = decodedState.getClaim("returnUrl").asString();

        this.request.setAttribute(ErrorRedirect.RETURN_URL, returnUrl);

        if (error != null) {
            throw new HangarApiException("OAuth error: " + error + " - " + errorDescription);
        }

        if (code != null) {
            final OAuthCodeResponse response = this.oAuthService.redeemCode(provider, code);
            final OAuthUserDetails userDetails = this.oAuthService.getUserDetails(provider, response);

            switch (mode) {
                case LOGIN, SIGNUP -> {
                    final UserTable existingUser = this.oAuthService.login(provider, userDetails.id(), userDetails.username());
                    if (existingUser != null) {
                        this.authService.setAalAndLogin(existingUser, 2);
                        this.redirect(returnUrl);
                    } else {
                        // save the user details and let the user choose their name and email
                        final String jwt = this.oAuthService.registerState(userDetails, provider, returnUrl);
                        this.redirect("/auth/oauth-signup?state=" + jwt);
                    }
                }
                case SETTINGS -> {
                    final long userId = Long.parseLong(decodedState.getSubject());
                    this.oAuthService.registerCredentials(provider, userId, userDetails);
                    this.redirect(returnUrl);
                }
            }
        } else {
            this.redirect(returnUrl);
        }
    }

    @PostMapping("/register")
    public OAuthSignupResponse register(@RequestBody final OAuthSignupForm form) {
        final DecodedJWT decodedState = this.tokenService.verify(form.jwt());
        final String oauthId = decodedState.getSubject();
        final String oauthEmail = decodedState.getClaim("email").asString();
        final String oauthUsername = decodedState.getClaim("username").asString();
        final String oauthProvider = decodedState.getClaim("provider").asString();

        final String email = form.email();
        final String username = form.username();
        final boolean tos = form.tos();

        final UserTable newUser = this.oAuthService.register(oauthProvider, oauthId, username, email, tos, oauthEmail.equals(email));
        this.oAuthService.registerCredentials(oauthProvider, newUser.getUserId(), new OAuthUserDetails(oauthId, oauthUsername, oauthEmail));
        this.authService.setAalAndLogin(newUser, 2);
        return new OAuthSignupResponse(!newUser.isEmailVerified());
    }

    @RequireAal(2)
    @PostMapping("/{provider}/unlink/{id}")
    public String unlink(@PathVariable final String provider, @PathVariable final String id) {
        return this.oAuthService.unlink(provider, id);
    }

    private void redirect(String returnUrl) throws IOException {
        if (returnUrl == null) {
            returnUrl = "/";
        }
        if (this.config.dev() && !returnUrl.startsWith("http")) {
            returnUrl = "http://localhost:3333" + returnUrl;
        }
        this.response.sendRedirect(returnUrl);
    }
}
