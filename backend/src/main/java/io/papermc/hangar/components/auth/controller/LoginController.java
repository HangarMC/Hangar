package io.papermc.hangar.components.auth.controller;

import com.yubico.webauthn.AssertionRequest;
import com.yubico.webauthn.AssertionResult;
import com.yubico.webauthn.FinishAssertionOptions;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.data.PublicKeyCredential;
import com.yubico.webauthn.exception.AssertionFailedException;
import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.components.auth.model.credential.CredentialType;
import io.papermc.hangar.components.auth.model.credential.WebAuthNCredential;
import io.papermc.hangar.components.auth.model.dto.login.LoginBackupForm;
import io.papermc.hangar.components.auth.model.dto.login.LoginPasswordForm;
import io.papermc.hangar.components.auth.model.dto.login.LoginResponse;
import io.papermc.hangar.components.auth.model.dto.login.LoginTotpForm;
import io.papermc.hangar.components.auth.model.dto.login.LoginWebAuthNForm;
import io.papermc.hangar.components.auth.service.AuthService;
import io.papermc.hangar.components.auth.service.CredentialsService;
import io.papermc.hangar.components.auth.service.WebAuthNService;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.security.annotations.Anyone;
import io.papermc.hangar.security.annotations.ratelimit.RateLimit;
import io.papermc.hangar.security.annotations.unlocked.Unlocked;
import io.papermc.hangar.security.authentication.HangarPrincipal;
import io.papermc.hangar.service.internal.users.UserService;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RateLimit(path = "auth")
@RequestMapping(path = "/api/internal/auth", produces = MediaType.APPLICATION_JSON_VALUE)
public class LoginController extends HangarComponent {

    private final UserService userService;
    private final CredentialsService credentialsService;
    private final RelyingParty relyingParty;
    private final WebAuthNService webAuthNService;
    private final AuthService authService;

    public LoginController(final UserService userService, final CredentialsService credentialsService, final RelyingParty relyingParty, final WebAuthNService webAuthNService, final AuthService authService) {
        this.userService = userService;
        this.credentialsService = credentialsService;
        this.relyingParty = relyingParty;
        this.webAuthNService = webAuthNService;
        this.authService = authService;
    }

    @Unlocked
    @PostMapping("login/sudo")
    public LoginResponse loginSudo() {
        final List<CredentialType> types = this.credentialsService.getCredentialTypes(this.getHangarPrincipal().getUserId());
        return new LoginResponse(this.getHangarPrincipal().getAal(), types, null);
    }

    @Anyone
    @PostMapping("login/password")
    public LoginResponse loginPassword(@RequestBody final LoginPasswordForm form) {
        final UserTable userTable = this.verifyPassword(form.usernameOrEmail(), form.password());
        final List<CredentialType> types = this.credentialsService.getCredentialTypes(userTable.getUserId());
        final int aal = userTable.isEmailVerified() ? 1 : 0;
        if (types.isEmpty() || (types.size() == 1 && types.getFirst() == CredentialType.BACKUP_CODES)) {
            return this.authService.setAalAndLogin(userTable, aal);
        } else {
            return new LoginResponse(aal, types, null);
        }
    }

    @Anyone
    @PostMapping("login/webauthn")
    public LoginResponse loginWebAuthN(@RequestBody final LoginWebAuthNForm form) throws IOException {
        final Optional<HangarPrincipal> principal = this.getOptionalHangarPrincipal();
        final UserTable userTable;
        //noinspection OptionalIsPresent
        if (principal.isPresent()) {
            userTable = Objects.requireNonNull(this.userService.getUserTable(principal.get().getUserId()));
        } else {
            userTable = this.verifyPassword(form.usernameOrEmail(), form.password());
        }

        final var pkc = PublicKeyCredential.parseAssertionResponseJson(form.publicKeyCredentialJson());

        final WebAuthNCredential.PendingLogin pendingLogin = this.webAuthNService.retrieveLoginRequest(userTable.getUserId());
        try {
            final AssertionResult result = this.relyingParty.finishAssertion(FinishAssertionOptions.builder()
                .request(AssertionRequest.fromJson(pendingLogin.json()))
                .response(pkc)
                .build());

            this.webAuthNService.updateCredential(userTable.getUserId(), result.getCredential().getCredentialId().getBase64(), result.getSignatureCount());

            if (result.isSuccess()) {
                return this.authService.setAalAndLogin(userTable, 2);
            }
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Bad credentials");
        } catch (final AssertionFailedException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Bad credentials");
        }
    }

    @Anyone
    @PostMapping("login/totp")
    public LoginResponse loginTotp(@RequestBody final LoginTotpForm form) {
        final Optional<HangarPrincipal> principal = this.getOptionalHangarPrincipal();
        if (principal.isPresent()) {
            this.credentialsService.verifyTotp(principal.get().getUserId(), form.totpCode());
            return this.authService.setAalAndLogin(Objects.requireNonNull(this.userService.getUserTable(principal.get().getUserId())), 2);
        }

        final UserTable userTable = this.verifyPassword(form.usernameOrEmail(), form.password());
        this.credentialsService.verifyTotp(userTable.getUserId(), form.totpCode());
        return this.authService.setAalAndLogin(userTable, 2);
    }

    @Anyone
    @PostMapping("login/backup")
    public LoginResponse loginBackup(@RequestBody final LoginBackupForm form) {
        final Optional<HangarPrincipal> principal = this.getOptionalHangarPrincipal();
        if (principal.isPresent()) {
            this.credentialsService.verifyBackupCode(principal.get().getUserId(), form.backupCode());
            return this.authService.setAalAndLogin(Objects.requireNonNull(this.userService.getUserTable(principal.get().getUserId())), 2);
        }

        final UserTable userTable = this.verifyPassword(form.usernameOrEmail(), form.password());
        this.credentialsService.verifyBackupCode(userTable.getUserId(), form.backupCode());
        return this.authService.setAalAndLogin(userTable, 2);
    }

    private UserTable verifyPassword(final String usernameOrEmail, final String password) {
        final UserTable userTable = this.userService.getUserTable(usernameOrEmail);
        if (userTable == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }

        this.credentialsService.verifyPassword(userTable.getUserId(), password);
        return userTable;
    }
}
