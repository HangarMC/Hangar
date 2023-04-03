package io.papermc.hangar.components.auth.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.yubico.webauthn.AssertionRequest;
import com.yubico.webauthn.FinishRegistrationOptions;
import com.yubico.webauthn.RegistrationResult;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.StartAssertionOptions;
import com.yubico.webauthn.StartRegistrationOptions;
import com.yubico.webauthn.data.AuthenticatorSelectionCriteria;
import com.yubico.webauthn.data.PublicKeyCredential;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;
import com.yubico.webauthn.data.ResidentKeyRequirement;
import com.yubico.webauthn.data.UserIdentity;
import com.yubico.webauthn.exception.RegistrationFailedException;
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
import io.papermc.hangar.components.auth.model.credential.WebAuthNCredential;
import io.papermc.hangar.components.auth.model.db.UserCredentialTable;
import io.papermc.hangar.components.auth.model.dto.ResetForm;
import io.papermc.hangar.components.auth.model.dto.TotpForm;
import io.papermc.hangar.components.auth.model.dto.TotpSetupResponse;
import io.papermc.hangar.components.auth.model.dto.WebAuthNSetupResponse;
import io.papermc.hangar.components.auth.service.AuthService;
import io.papermc.hangar.components.auth.service.CredentialsService;
import io.papermc.hangar.components.auth.service.VerificationService;
import io.papermc.hangar.components.auth.service.WebAuthNService;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.security.annotations.Anyone;
import io.papermc.hangar.security.annotations.ratelimit.RateLimit;
import io.papermc.hangar.security.annotations.unlocked.Unlocked;
import io.papermc.hangar.service.internal.users.UserService;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;

import static dev.samstevens.totp.util.Utils.getDataUriForImage;

@Controller
@RateLimit(path = "auth")
@RequestMapping(path = "/api/internal/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@ResponseBody
public class CredentialController extends HangarComponent {

    private final SecretGenerator secretGenerator;
    private final QrDataFactory qrDataFactory;
    private final QrGenerator qrGenerator;
    private final RecoveryCodeGenerator recoveryCodeGenerator;
    private final CodeVerifier codeVerifier;
    private final AuthService authService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final VerificationService verificationService;
    private final CredentialsService credentialsService;
    private final RelyingParty relyingParty;
    private final WebAuthNService webAuthNService;

    public CredentialController(final SecretGenerator secretGenerator, final QrDataFactory qrDataFactory, final QrGenerator qrGenerator, final RecoveryCodeGenerator recoveryCodeGenerator, final CodeVerifier codeVerifier, final AuthService authService, final UserService userService, final PasswordEncoder passwordEncoder, final VerificationService verificationService, final CredentialsService credentialsService, final RelyingParty relyingParty, final WebAuthNService webAuthNService) {
        this.secretGenerator = secretGenerator;
        this.qrDataFactory = qrDataFactory;
        this.qrGenerator = qrGenerator;
        this.recoveryCodeGenerator = recoveryCodeGenerator;
        this.codeVerifier = codeVerifier;
        this.authService = authService;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.verificationService = verificationService;
        this.credentialsService = credentialsService;
        this.relyingParty = relyingParty;
        this.webAuthNService = webAuthNService;
    }

    /*
     * WEBAUTHN
     */

    @Unlocked
    @PostMapping(value = "/webauthn/setup", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.TEXT_PLAIN_VALUE)
    public String setupWebauthn(@RequestBody final String authenticatorName) throws JsonProcessingException {
        final UserIdentity userIdentity = this.webAuthNService.getExistingUserOrCreate(this.getHangarPrincipal().getUserId(), this.getHangarPrincipal().getName());

        final WebAuthNSetupResponse response = new WebAuthNSetupResponse(
            this.getHangarPrincipal().getName(),
            authenticatorName,
            this.webAuthNService.generateRandom(32),
            this.relyingParty.startRegistration(
                StartRegistrationOptions.builder()
                    .user(userIdentity)
                    .authenticatorSelection(
                        AuthenticatorSelectionCriteria.builder()
                            .residentKey(ResidentKeyRequirement.DISCOURAGED)
                            .build())
                    .build()));

        this.webAuthNService.storeSetupRequest(this.getHangarPrincipal().getUserId(), response.publicKeyCredentialCreationOptions().toJson(), authenticatorName);

        return response.publicKeyCredentialCreationOptions().toCredentialsCreateJson();
    }

    @Unlocked
    @PostMapping(value = "/webauthn/register", consumes = MediaType.TEXT_PLAIN_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public void registerWebauthn(@RequestBody final String publicKeyCredentialJson) throws IOException {
        final var pkc = PublicKeyCredential.parseRegistrationResponseJson(publicKeyCredentialJson);

        final WebAuthNCredential.PendingSetup pending = this.webAuthNService.retrieveSetupRequest(this.getHangarPrincipal().getUserId());

        final RegistrationResult registrationResult;
        try {
            registrationResult = this.relyingParty.finishRegistration(FinishRegistrationOptions.builder()
                .request(PublicKeyCredentialCreationOptions.fromJson(pending.json()))
                .response(pkc)
                .build());
        } catch (final RegistrationFailedException e) {
            this.logger.warn("Registration failed", e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        final String displayName = pending.authenticatorName();
        final String id = registrationResult.getKeyId().getId().getBase64();
        final String addedAt = OffsetDateTime.now().format(DateTimeFormatter.ISO_INSTANT);
        final String publicKey = registrationResult.getPublicKeyCose().getBase64();
        final WebAuthNCredential.Authenticator authenticator = new WebAuthNCredential.Authenticator(registrationResult.getAaguid().getBase64(), registrationResult.getSignatureCount(), false);
        final WebAuthNCredential.WebAuthNDevice device = new WebAuthNCredential.WebAuthNDevice(id, addedAt, publicKey, displayName, authenticator, false, "none");
        this.webAuthNService.addDevice(this.getHangarPrincipal().getUserId(), device);
    }

    @Anyone
    @PostMapping(value = "/webauthn/assert", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.TEXT_PLAIN_VALUE)
    public String prepareWebauthnLogin(@RequestBody final String username) throws JsonProcessingException {
        final UserTable userTable = this.userService.getUserTable(username);
        if (userTable == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        final AssertionRequest assertionRequest = this.relyingParty.startAssertion(StartAssertionOptions.builder().username(username).build());
        this.webAuthNService.storeLoginRequest(userTable.getUserId(), assertionRequest.toJson());
        return assertionRequest.toCredentialsGetJson();
    }

    @Unlocked
    @PostMapping(value = "/webauthn/unregister", consumes = MediaType.TEXT_PLAIN_VALUE)
    public void unregisterWebauthnDevice(@RequestBody final String id) {
        this.webAuthNService.removeDevice(this.getHangarPrincipal().getUserId(), id);
    }

    /*
     * TOTP
     */

    @Unlocked
    @PostMapping("/totp/setup")
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

        this.credentialsService.registerCredential(this.getHangarPrincipal().getUserId(), new TotpCredential(totpUrl));

        return ResponseEntity.ok().build();
    }

    @Unlocked
    @PostMapping("/totp/remove")
    @ResponseStatus(HttpStatus.OK)
    public void removeTotp() {
        // TODO security protection
        this.credentialsService.removeCredential(this.getHangarPrincipal().getId(), CredentialType.TOTP);
    }

    @Unlocked
    @PostMapping("/totp/verify")
    @ResponseStatus(HttpStatus.OK)
    public void verifyTotp(@RequestBody final String code) {
        this.credentialsService.verifyTotp(this.getHangarPrincipal().getUserId(), code);
    }

    /*
     * BACKUP CODES
     */

    @Unlocked
    @PostMapping("/codes/setup")
    public List<BackupCodeCredential.BackupCode> setupBackupCodes() {
        // TODO check if there are unconfirmed one in db first
        final List<BackupCodeCredential.BackupCode> codes = Arrays.stream(this.recoveryCodeGenerator.generateCodes(12)).map(s -> new BackupCodeCredential.BackupCode(s, null)).toList();
        this.credentialsService.registerCredential(this.getHangarPrincipal().getUserId(), new BackupCodeCredential(codes, true));
        return codes;
    }

    @Unlocked
    @PostMapping("/codes/show")
    public ResponseEntity<?> showBackupCodes() {
        // TODO security protection
        final UserCredentialTable table = this.credentialsService.getCredential(this.getHangarPrincipal().getId(), CredentialType.BACKUP_CODES);
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
        final UserCredentialTable table = this.credentialsService.getCredential(this.getHangarPrincipal().getId(), CredentialType.BACKUP_CODES);
        if (table == null) {
            return ResponseEntity.notFound().build();
        }
        BackupCodeCredential cred = table.getCredential().get(BackupCodeCredential.class);
        cred = new BackupCodeCredential(cred.backupCodes(), false);
        this.credentialsService.updateCredential(this.getHangarPrincipal().getId(), cred);
        return ResponseEntity.ok().build();
    }

    @Unlocked
    @PostMapping("/codes/regenerate")
    public List<BackupCodeCredential.BackupCode> regenerateBackupCodes() {
        // TODO security protection
        this.credentialsService.removeCredential(this.getHangarPrincipal().getId(), CredentialType.BACKUP_CODES);
        return this.setupBackupCodes();
    }

    /*
     * PW RESET
     */

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
        final UserTable userTable = this.userService.getUserTable(form.email());
        if (userTable != null) {
            if (this.authService.validPassword(form.password(), userTable.getName()) && this.verificationService.verifyResetCode(form.email(), form.code(), true)) {
                this.credentialsService.updateCredential(userTable.getUserId(), new PasswordCredential(this.passwordEncoder.encode(form.password())));
                return ResponseEntity.ok().build();
            }
        }
        return ResponseEntity.badRequest().build();
    }

    /*
     * EMAIL VERIFY
     */

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
}
