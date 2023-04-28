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
import io.papermc.hangar.components.auth.service.TokenService;
import io.papermc.hangar.components.auth.service.VerificationService;
import io.papermc.hangar.components.auth.service.WebAuthNService;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.exceptions.HangarResponseException;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.security.annotations.Anyone;
import io.papermc.hangar.security.annotations.aal.RequireAal;
import io.papermc.hangar.security.annotations.privileged.Privileged;
import io.papermc.hangar.security.annotations.ratelimit.RateLimit;
import io.papermc.hangar.security.annotations.unlocked.Unlocked;
import io.papermc.hangar.service.internal.users.UserService;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
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
    private final TokenService tokenService;

    public CredentialController(final SecretGenerator secretGenerator, final QrDataFactory qrDataFactory, final QrGenerator qrGenerator, final RecoveryCodeGenerator recoveryCodeGenerator, final CodeVerifier codeVerifier, final AuthService authService, final UserService userService, final PasswordEncoder passwordEncoder, final VerificationService verificationService, final CredentialsService credentialsService, final RelyingParty relyingParty, final WebAuthNService webAuthNService, final TokenService tokenService) {
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
        this.tokenService = tokenService;
    }

    /*
     * WEBAUTHN
     */

    @Privileged
    @RequireAal(1)
    @PostMapping(value = "/webauthn/setup", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.TEXT_PLAIN_VALUE)
    public String setupWebauthn(@RequestBody final String authenticatorName) throws JsonProcessingException {
        // TODO verify that backup codes exist
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

    @Privileged
    @RequireAal(1)
    @PostMapping(value = "/webauthn/register", consumes = MediaType.TEXT_PLAIN_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public void registerWebauthn(@RequestBody final String publicKeyCredentialJson, @RequestHeader(value = "X-Hangar-Verify", required = false) final String header) throws IOException {
        final boolean confirmCodes = this.verifyBackupCodes(header);

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

        if (confirmCodes) {
            this.confirmBackupCredential();
        }
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

    @Privileged
    @RequireAal(1)
    @PostMapping(value = "/webauthn/unregister", consumes = MediaType.TEXT_PLAIN_VALUE)
    public void unregisterWebauthnDevice(@RequestBody final String id) {
        this.webAuthNService.removeDevice(this.getHangarPrincipal().getUserId(), id);
        this.credentialsService.checkRemoveBackupCodes();
    }

    /*
     * TOTP
     */

    @Privileged
    @RequireAal(1)
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

    @Privileged
    @RequireAal(1)
    @PostMapping("/totp/register")
    public ResponseEntity<?> registerTotp(@RequestBody final TotpForm form, @RequestHeader(value = "X-Hangar-Verify", required = false) final String header) {
        final boolean confirmCodes = this.verifyBackupCodes(header);
        if (!StringUtils.hasText(form.code())) {
            throw new HangarApiException("Code is required");
        }

        if (!StringUtils.hasText(form.secret()) || !this.codeVerifier.isValidCode(form.secret(), form.code()) || !this.tokenService.verifyOtp(this.getHangarPrincipal().getUserId(), header)) {
            throw new HangarApiException("Invalid TOTP code");
        }

        final String totpUrl = this.qrDataFactory.newBuilder()
            .label(this.getHangarPrincipal().getName())
            .secret(form.secret())
            .issuer("Hangar")
            .build().getUri();

        this.credentialsService.registerCredential(this.getHangarPrincipal().getUserId(), new TotpCredential(totpUrl));

        if (confirmCodes) {
            this.confirmBackupCredential();
        }

        return ResponseEntity.ok().build();
    }

    @Privileged
    @RequireAal(1)
    @PostMapping("/totp/remove")
    @ResponseStatus(HttpStatus.OK)
    public void removeTotp() {
        this.credentialsService.removeCredential(this.getHangarPrincipal().getId(), CredentialType.TOTP);
        this.credentialsService.checkRemoveBackupCodes();
    }

    @Privileged
    @RequireAal(1)
    @PostMapping("/totp/verify")
    @ResponseStatus(HttpStatus.OK)
    public void verifyTotp(@RequestBody final String code) {
        this.credentialsService.verifyTotp(this.getHangarPrincipal().getUserId(), code);
    }

    /*
     * BACKUP CODES
     */

    private BackupCodeCredential getBackupCredential() {
        final UserCredentialTable table = this.credentialsService.getCredential(this.getHangarPrincipal().getId(), CredentialType.BACKUP_CODES);
        if (table == null) {
            return null;
        }
        return table.getCredential().get(BackupCodeCredential.class);
    }

    private void confirmBackupCredential() {
        BackupCodeCredential cred = this.getBackupCredential();
        if (cred == null) {
            throw new HangarApiException("No pending codes");
        }
        cred = new BackupCodeCredential(cred.backupCodes(), false);
        this.credentialsService.updateCredential(this.getHangarPrincipal().getId(), cred);
    }

    private boolean verifyBackupCodes(final String header) {
        final BackupCodeCredential backupCredential = this.getBackupCredential();
        if (backupCredential == null) {
            // no codes yet? we generate some
            final HttpHeaders headers = new HttpHeaders();
            headers.set("X-Hangar-Verify", this.tokenService.otp(this.getHangarPrincipal().getUserId()));
            throw new HangarResponseException(HttpStatusCode.valueOf(499), "Setup backup codes first", this.setupBackupCodes(true), headers);
        } else if (backupCredential.unconfirmed()) {
            if (StringUtils.hasText(header)) {
                if (!backupCredential.matches(header.split(":")[0])) {
                    // wrong code? -> proper error
                    throw new HangarApiException("Backup code doesn't match");
                }
                // only if unconfirmed + code is right we mark for confirm
                return true;
            } else {
                // unconfirmed codes? better enter the code!
                final HttpHeaders headers = new HttpHeaders();
                headers.set("X-Hangar-Verify", this.tokenService.otp(this.getHangarPrincipal().getUserId()));
                throw new HangarResponseException(HttpStatusCode.valueOf(499), "Confirm backup codes first", backupCredential.backupCodes(), headers);
            }
        }
        return false;
    }

    private List<BackupCodeCredential.BackupCode> setupBackupCodes(final boolean unconfirmed) {
        final List<BackupCodeCredential.BackupCode> codes = Arrays.stream(this.recoveryCodeGenerator.generateCodes(12)).map(s -> new BackupCodeCredential.BackupCode(s, null)).toList();
        this.credentialsService.registerCredential(this.getHangarPrincipal().getUserId(), new BackupCodeCredential(codes, unconfirmed));
        return codes;
    }

    @Privileged
    @RequireAal(1)
    @PostMapping("/codes/show")
    public List<BackupCodeCredential.BackupCode> showBackupCodes() {
        final BackupCodeCredential cred = this.getBackupCredential();
        if (cred == null || cred.unconfirmed()) {
            throw new HangarApiException("You haven't setup backup codes");
        }
        return cred.backupCodes();
    }

    @Privileged
    @RequireAal(1)
    @PostMapping("/codes/regenerate")
    public List<BackupCodeCredential.BackupCode> regenerateBackupCodes() {
        this.credentialsService.removeCredential(this.getHangarPrincipal().getId(), CredentialType.BACKUP_CODES);
        return this.setupBackupCodes(false); //TODO Require confirmation, otherwise don't delete the old codes
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
