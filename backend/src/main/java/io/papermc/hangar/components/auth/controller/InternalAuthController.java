package io.papermc.hangar.components.auth.controller;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
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
import io.papermc.hangar.components.auth.model.dto.SignupForm;
import io.papermc.hangar.components.auth.model.dto.TotpSetupResponse;
import io.papermc.hangar.components.auth.service.AuthService;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.security.annotations.Anyone;
import io.papermc.hangar.security.annotations.ratelimit.RateLimit;
import io.papermc.hangar.security.annotations.unlocked.Unlocked;
import io.papermc.hangar.components.auth.model.dto.webauthn.AuthenticatorForm;
import io.papermc.hangar.components.auth.service.WebAuthNService;
import io.papermc.hangar.security.configs.SecurityConfig;
import io.papermc.hangar.service.TokenService;
import jakarta.servlet.http.HttpSession;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.view.RedirectView;

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

    public InternalAuthController(final WebAuthNService webAuthNService, final SecretGenerator secretGenerator, final QrDataFactory qrDataFactory, final QrGenerator qrGenerator, final RecoveryCodeGenerator recoveryCodeGenerator, final CodeVerifier codeVerifier, final AuthService authService, final TokenService tokenService) {
        this.webAuthNService = webAuthNService;
        this.secretGenerator = secretGenerator;
        this.qrDataFactory = qrDataFactory;
        this.qrGenerator = qrGenerator;
        this.recoveryCodeGenerator = recoveryCodeGenerator;
        this.codeVerifier = codeVerifier;
        this.authService = authService;
        this.tokenService = tokenService;
    }

    // TODO have a table with credentials (type pw, backup code, totp, webauthn)

    @Anyone
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody final SignupForm signupForm) {
        // TODO signup
        final UserTable userTable = this.authService.registerUser(signupForm);
        if (userTable == null) {
            return ResponseEntity.badRequest().build();
        }
        this.tokenService.issueRefreshToken(userTable.getUserId(), this.response);
        return ResponseEntity.ok().build();
    }

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

        // TODO store the secret

        // TODO proper data
        final QrData data = this.qrDataFactory.newBuilder()
            .label("example@example.com")
            .secret(secret)
            .issuer("AppName")
            .build();

        final String qrCodeImage = getDataUriForImage(
            this.qrGenerator.generate(data),
            this.qrGenerator.getImageMimeType()
        );

        return new TotpSetupResponse(secret, qrCodeImage);
    }

    @Unlocked
    @PostMapping("/totp/register")
    public void registerTotp() {
        // TODO totp
    }

    @Unlocked
    @PostMapping("/totp/remove")
    public void removeTotp() {
        // TODO totp
    }

    @Unlocked
    @PostMapping("/totp/verify")
    public ResponseEntity<?> verifyTotp(@RequestBody final String code) {
        final String secret = "";
        // TODO load secret
        if (this.codeVerifier.isValidCode(secret, code)) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @Unlocked
    @PostMapping("/code/register")
    public void registerBackupCodes() {
        // TODO backup codes
        this.recoveryCodeGenerator.generateCodes(10);
        // TODO return one and force the user to confirm?
    }
}
