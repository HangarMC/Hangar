package io.papermc.hangar.components.auth.controller;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.components.auth.model.credential.BackupCodeCredential;
import io.papermc.hangar.components.auth.model.credential.CredentialType;
import io.papermc.hangar.components.auth.model.credential.WebAuthNCredential;
import io.papermc.hangar.components.auth.model.db.UserCredentialTable;
import io.papermc.hangar.components.auth.model.db.VerificationCodeTable;
import io.papermc.hangar.components.auth.model.dto.AccountForm;
import io.papermc.hangar.components.auth.model.dto.SettingsResponse;
import io.papermc.hangar.components.auth.model.dto.SignupForm;
import io.papermc.hangar.components.auth.service.AuthService;
import io.papermc.hangar.components.auth.service.CredentialsService;
import io.papermc.hangar.components.auth.service.TokenService;
import io.papermc.hangar.components.auth.service.VerificationService;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.security.annotations.Anyone;
import io.papermc.hangar.security.annotations.ratelimit.RateLimit;
import io.papermc.hangar.security.annotations.unlocked.Unlocked;
import io.papermc.hangar.security.configs.SecurityConfig;
import io.papermc.hangar.service.internal.users.UserService;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@RateLimit(path = "auth")
@RequestMapping(path = "/api/internal/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@ResponseBody
public class AuthController extends HangarComponent {

    private final AuthService authService;
    private final TokenService tokenService;
    private final VerificationService verificationService;
    private final CredentialsService credentialsService;
    private final UserService userService;

    public AuthController(final AuthService authService, final TokenService tokenService, final VerificationService verificationService, final CredentialsService credentialsService, final UserService userService) {
        this.authService = authService;
        this.tokenService = tokenService;
        this.verificationService = verificationService;
        this.credentialsService = credentialsService;
        this.userService = userService;
    }

    @Anyone
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody final SignupForm signupForm) {
        final UserTable userTable = this.authService.registerUser(signupForm);
        if (userTable == null) {
            return ResponseEntity.badRequest().build();
        }
        // TODO proper session handling with devices list and shit
        this.tokenService.issueRefreshToken(userTable.getUserId(), this.response);
        return ResponseEntity.ok().build();
    }

    @Anyone
    @GetMapping("/logout")
    public void loggedOut(@CookieValue(name = SecurityConfig.REFRESH_COOKIE_NAME, required = false) final String refreshToken) {
        // invalidate refresh token
        if (refreshToken != null) {
            this.tokenService.invalidateToken(refreshToken);
        }
        // invalidate session
        final HttpSession session = this.request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    @Anyone
    @ResponseBody
    @GetMapping("/refresh")
    public String refreshAccessToken(@CookieValue(name = SecurityConfig.REFRESH_COOKIE_NAME, required = false) final String refreshToken) {
        return this.tokenService.refreshAccessToken(refreshToken).accessToken();
    }

    @Anyone
    @GetMapping("/invalidate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void invalidateRefreshToken(@CookieValue(name = SecurityConfig.REFRESH_COOKIE_NAME, required = false) final String refreshToken) {
        this.tokenService.invalidateToken(refreshToken);
        final HttpSession session = this.request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    @Unlocked
    @PostMapping("/settings")
    public SettingsResponse settings() {
        final long userId = this.getHangarPrincipal().getUserId();
        final List<SettingsResponse.Authenticator> authenticators = this.getAuthenticators(userId);
        final UserCredentialTable backupCodeTable = this.credentialsService.getCredential(userId, CredentialType.BACKUP_CODES);
        boolean hasBackupCodes = false;
        if (backupCodeTable != null) {
            final BackupCodeCredential backupCodeCredential = backupCodeTable.getCredential().get(BackupCodeCredential.class);
            if (backupCodeCredential != null && !backupCodeCredential.unconfirmed()) {
                hasBackupCodes = true;
            }
        }
        final boolean hasTotp = this.credentialsService.getCredential(userId, CredentialType.TOTP) != null;
        final boolean emailVerified = Objects.requireNonNull(this.userService.getUserTable(userId)).isEmailVerified();
        final VerificationCodeTable verificationCode = this.verificationService.getVerificationCode(userId, VerificationCodeTable.VerificationCodeType.EMAIL_VERIFICATION);
        final boolean emailPending = verificationCode != null && !this.verificationService.expired(verificationCode);
        return new SettingsResponse(authenticators, hasBackupCodes, hasTotp, emailVerified, emailPending);
    }

    private List<SettingsResponse.Authenticator> getAuthenticators(final long userId) {
        final UserCredentialTable credential = this.credentialsService.getCredential(userId, CredentialType.WEBAUTHN);
        if (credential != null) {
            final WebAuthNCredential webAuthNCredential = credential.getCredential().get(WebAuthNCredential.class);
            if (webAuthNCredential != null && webAuthNCredential.credentials() != null) {
                return webAuthNCredential.credentials().stream()
                    .map(c -> new SettingsResponse.Authenticator(c.addedAt(), c.displayName(), c.id()))
                    .toList();
            }
        }
        return List.of();
    }

    @Unlocked
    @PostMapping("/account")
    public void saveAccount(@RequestBody final AccountForm form) {
        this.credentialsService.verifyPassword(this.getHangarPrincipal().getUserId(), form.currentPassword());

        final UserTable userTable = this.userService.getUserTable(this.getHangarPrincipal().getUserId());
        if (userTable == null) {
            throw new HangarApiException("No user?!");
        }

        if (!this.getHangarPrincipal().getUsername().equals(form.username())) {
            this.authService.handleUsernameChange(userTable, form.username());
        }

        if (!this.getHangarPrincipal().getEmail().equals(form.email())) {
            this.authService.handleEmailChange(userTable, form.email());
        }

        if (StringUtils.hasText(form.newPassword())) {
            this.authService.handlePasswordChange(userTable, form.newPassword());
        }
    }
}
