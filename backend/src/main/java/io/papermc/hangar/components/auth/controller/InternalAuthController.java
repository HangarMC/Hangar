package io.papermc.hangar.components.auth.controller;

import com.webauthn4j.data.AuthenticatorTransport;
import com.webauthn4j.data.attestation.AttestationObject;
import com.webauthn4j.springframework.security.authenticator.WebAuthnAuthenticatorImpl;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrDataFactory;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.recovery.RecoveryCodeGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.components.auth.model.credential.BackupCodeCredential;
import io.papermc.hangar.components.auth.model.credential.CredentialType;
import io.papermc.hangar.components.auth.model.credential.PasswordCredential;
import io.papermc.hangar.components.auth.model.credential.TotpCredential;
import io.papermc.hangar.components.auth.model.db.UserCredentialTable;
import io.papermc.hangar.components.auth.model.db.VerificationCodeTable;
import io.papermc.hangar.components.auth.model.dto.ResetForm;
import io.papermc.hangar.components.auth.model.dto.SettingsResponse;
import io.papermc.hangar.components.auth.model.dto.SignupForm;
import io.papermc.hangar.components.auth.model.dto.TotpForm;
import io.papermc.hangar.components.auth.model.dto.TotpSetupResponse;
import io.papermc.hangar.components.auth.service.AuthService;
import io.papermc.hangar.components.auth.service.VerificationService;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.security.annotations.Anyone;
import io.papermc.hangar.security.annotations.ratelimit.RateLimit;
import io.papermc.hangar.security.annotations.unlocked.Unlocked;
import io.papermc.hangar.components.auth.model.dto.webauthn.AuthenticatorForm;
import io.papermc.hangar.components.auth.service.WebAuthNService;
import io.papermc.hangar.security.configs.SecurityConfig;
import io.papermc.hangar.components.auth.service.TokenService;
import io.papermc.hangar.service.internal.users.UserService;
import jakarta.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.util.UriComponentsBuilder;

import static dev.samstevens.totp.util.Utils.getDataUriForImage;

@Controller
@RateLimit(path = "auth")
@RequestMapping(path = "/api/internal/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@ResponseBody
public class InternalAuthController extends HangarComponent {

    private final WebAuthNService webAuthNService;
    private final SecretGenerator secretGenerator;
    private final QrDataFactory qrDataFactory;
    private final QrGenerator qrGenerator;
    private final RecoveryCodeGenerator recoveryCodeGenerator;
    private final CodeVerifier codeVerifier;
    private final AuthService authService;
    private final TokenService tokenService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final VerificationService verificationService;

    public InternalAuthController(final WebAuthNService webAuthNService, final SecretGenerator secretGenerator, final QrDataFactory qrDataFactory, final QrGenerator qrGenerator, final RecoveryCodeGenerator recoveryCodeGenerator, final CodeVerifier codeVerifier, final AuthService authService, final TokenService tokenService, final UserService userService, final PasswordEncoder passwordEncoder, final VerificationService verificationService) {
        this.webAuthNService = webAuthNService;
        this.secretGenerator = secretGenerator;
        this.qrDataFactory = qrDataFactory;
        this.qrGenerator = qrGenerator;
        this.recoveryCodeGenerator = recoveryCodeGenerator;
        this.codeVerifier = codeVerifier;
        this.authService = authService;
        this.tokenService = tokenService;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.verificationService = verificationService;
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

    // TODO redo webauth, needs to use credentials system, we prolly dont care about the default spring shit, just do it like totp, manually
    @Unlocked
    @PostMapping("/webauthn/register")
    @ResponseStatus(HttpStatus.OK)
    public void registerAuthenticator(@RequestBody final AuthenticatorForm form) {
        System.out.println("got form! " + form);
        final AttestationObject attestationObject = form.attestationObject().attestationObject();
        final Set<AuthenticatorTransport> transports;
        if (form.transports() != null) {
            transports = form.transports().stream().map(AuthenticatorTransport::create).collect(Collectors.toSet());
        } else {
            transports = Set.of();
        }
        final WebAuthnAuthenticatorImpl auth = new WebAuthnAuthenticatorImpl(form.name(), this.getHangarPrincipal(), attestationObject.getAuthenticatorData().getAttestedCredentialData(), attestationObject.getAttestationStatement(), 0, transports, null, null);
        this.webAuthNService.store(this.getHangarPrincipal().getName(), auth);
    }

    @Unlocked
    @GetMapping("/totp/setup")
    public TotpSetupResponse setupTotp() throws QrGenerationException {
        final String secret = this.secretGenerator.generate();

        final QrData data = this.qrDataFactory.newBuilder()
            .label(this.getHangarPrincipal().getName())
            .secret(secret)
            .issuer("Hangar")
            .build();

        final String qrCodeImage = getDataUriForImage(
            this.qrGenerator.generate(data),
            this.qrGenerator.getImageMimeType()
        );

        return new TotpSetupResponse(secret, qrCodeImage);
    }

    @Unlocked
    @PostMapping("/totp/register")
    public ResponseEntity<?> registerTotp(@RequestBody final TotpForm form) {
        if (!this.codeVerifier.isValidCode(form.secret(), form.code())) {
            return ResponseEntity.badRequest().build();
        }

        final String totpUrl = this.qrDataFactory.newBuilder()
            .label(this.getHangarPrincipal().getName())
            .secret(form.secret())
            .issuer("Hangar")
            .build().getUri();

        this.authService.registerCredential(this.getHangarPrincipal().getUserId(), new TotpCredential(totpUrl));

        return ResponseEntity.ok().build();
    }

    @Unlocked
    @PostMapping("/totp/remove")
    @ResponseStatus(HttpStatus.OK)
    public void removeTotp() {
        // TODO security protection
        this.authService.removeCredential(this.getHangarPrincipal().getId(), CredentialType.TOTP);
    }

    @Unlocked
    @PostMapping("/totp/verify")
    public ResponseEntity<?> verifyTotp(@RequestBody final String code) {
        final UserCredentialTable credential = this.authService.getCredential(this.getHangarPrincipal().getUserId(), CredentialType.TOTP);
        if (credential != null) {
            final String totpUrl = credential.getCredential().get(TotpCredential.class).totpUrl();
            final UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(totpUrl);
            final String secret = builder.build().getQueryParams().getFirst("secret");
            if (this.codeVerifier.isValidCode(secret, code)) {
                return ResponseEntity.ok().build();
            }
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @Unlocked
    @PostMapping("/codes/setup")
    public List<BackupCodeCredential.BackupCode> setupBackupCodes() {
        // TODO check if there are unconfirmed one in db first
        final List<BackupCodeCredential.BackupCode> codes = Arrays.stream(this.recoveryCodeGenerator.generateCodes(12)).map(s -> new BackupCodeCredential.BackupCode(s, null)).toList();
        this.authService.registerCredential(this.getHangarPrincipal().getUserId(), new BackupCodeCredential(codes, true));
        return codes;
    }

    @Unlocked
    @PostMapping("/codes/show")
    public ResponseEntity<?> showBackupCodes() {
        // TODO security protection
        final UserCredentialTable table = this.authService.getCredential(this.getHangarPrincipal().getId(), CredentialType.BACKUP_CODES);
        if (table == null) {
            return ResponseEntity.notFound().build();
        }
        final BackupCodeCredential cred = table.getCredential().get(BackupCodeCredential.class);
        if (cred == null || cred.unconfirmed()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(cred.backupCodes());
    }

    @Unlocked
    @PostMapping("/codes/register")
    public ResponseEntity<?> registerBackupCodes() {
        final UserCredentialTable table = this.authService.getCredential(this.getHangarPrincipal().getId(), CredentialType.BACKUP_CODES);
        if (table == null) {
            return ResponseEntity.notFound().build();
        }
        BackupCodeCredential cred = table.getCredential().get(BackupCodeCredential.class);
        cred = new BackupCodeCredential(cred.backupCodes(), false);
        this.authService.updateCredential(this.getHangarPrincipal().getId(), cred);
        return ResponseEntity.ok().build();
    }

    @Unlocked
    @PostMapping("/codes/regenerate")
    public List<BackupCodeCredential.BackupCode> regenerateBackupCodes() {
        // TODO security protection
        this.authService.removeCredential(this.getHangarPrincipal().getId(), CredentialType.BACKUP_CODES);
        return this.setupBackupCodes();
    }

    @Anyone
    @PostMapping("/reset/send")
    @ResponseStatus(HttpStatus.OK)
    public void sendResetMail(@RequestBody final ResetForm form) {
        this.verificationService.sendResetCode(form.email());
    }

    @Anyone
    @PostMapping("/reset/verify")
    public ResponseEntity<Object> verifyResetCode(@RequestBody final ResetForm form) {
        if (this.verificationService.verifyResetCode(form.email(), form.code(), false)) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @Anyone
    @PostMapping("/reset/set")
    public ResponseEntity<Object> setNewPassword(@RequestBody final ResetForm form) {
        if (this.authService.validPassword(form.password()) && this.verificationService.verifyResetCode(form.email(), form.code(), true)) {
            final UserTable userTable = this.userService.getUserTable(form.email());
            if (userTable != null) {
                this.authService.updateCredential(userTable.getUserId(), new PasswordCredential(this.passwordEncoder.encode(form.password())));
                return ResponseEntity.ok().build();
            }
        }
        return ResponseEntity.badRequest().build();
    }

    @Unlocked
    @PostMapping(value = "/email/verify", consumes = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<Object> verifyEmail(@RequestBody final String code) {
        if (this.verificationService.verifyEmailCode(this.getHangarPrincipal().getId(), code)) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @Unlocked
    @PostMapping("/email/send")
    @ResponseStatus(HttpStatus.OK)
    public void sendEmailCode() {
        this.verificationService.sendVerificationCode(this.getHangarPrincipal().getId(), this.getHangarPrincipal().getEmail(), this.getHangarPrincipal().getName());
    }

    @Unlocked
    @PostMapping("/settings")
    public SettingsResponse settings() {
        // TODO more (authenticators for example)
        final boolean hasBackupCodes = this.authService.getCredential(this.getHangarPrincipal().getUserId(), CredentialType.BACKUP_CODES) != null;
        final boolean hasTotp = this.authService.getCredential(this.getHangarPrincipal().getUserId(), CredentialType.TOTP) != null;
        final boolean emailVerified = this.getHangarPrincipal().isEmailVerified();
        final boolean emailPending = this.verificationService.getVerificationCode(this.getHangarPrincipal().getUserId(), VerificationCodeTable.VerificationCodeType.EMAIL_VERIFICATION) != null;
        return new SettingsResponse(hasBackupCodes, hasTotp, emailVerified, emailPending);
    }
}
