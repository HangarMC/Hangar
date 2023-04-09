package io.papermc.hangar.components.auth.service;

import com.yubico.webauthn.data.ByteArray;
import dev.samstevens.totp.code.CodeVerifier;
import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.components.auth.dao.UserCredentialDAO;
import io.papermc.hangar.components.auth.model.credential.BackupCodeCredential;
import io.papermc.hangar.components.auth.model.credential.Credential;
import io.papermc.hangar.components.auth.model.credential.CredentialType;
import io.papermc.hangar.components.auth.model.credential.PasswordCredential;
import io.papermc.hangar.components.auth.model.credential.TotpCredential;
import io.papermc.hangar.components.auth.model.db.UserCredentialTable;
import io.papermc.hangar.db.customtypes.JSONB;
import io.papermc.hangar.model.db.UserTable;
import java.util.List;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class CredentialsService extends HangarComponent {

    private final CodeVerifier codeVerifier;
    private final PasswordEncoder passwordEncoder;
    private final UserCredentialDAO userCredentialDAO;

    public CredentialsService(final CodeVerifier codeVerifier, final PasswordEncoder passwordEncoder, final UserCredentialDAO userCredentialDAO) {
        this.codeVerifier = codeVerifier;
        this.passwordEncoder = passwordEncoder;
        this.userCredentialDAO = userCredentialDAO;
    }

    public void registerCredential(final long userId, final Credential credential) {
        this.userCredentialDAO.insert(userId, new JSONB(credential), credential.type());
    }

    public void removeCredential(final long userId, final CredentialType type) {
        this.userCredentialDAO.remove(userId, type);
    }

    public void updateCredential(final long userId, final Credential credential) {
        this.userCredentialDAO.update(userId, new JSONB(credential), credential.type());
    }

    public @Nullable UserCredentialTable getCredential(final long userId, final CredentialType type) {
        return this.userCredentialDAO.getByType(type, userId);
    }

    public @Nullable UserCredentialTable getCredentialByUserHandle(final ByteArray userHandle) {
        return this.userCredentialDAO.getByUserHandle(CredentialType.WEBAUTHN, userHandle.getBase64());
    }

    public List<CredentialType> getCredentialTypes(final long userId) {
        return this.userCredentialDAO.getAll(userId, CredentialType.PASSWORD, CredentialType.WEBAUTHN);
    }

    public void verifyPassword(final long userId, final String password) {
        if (!StringUtils.hasText(password)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Bad credentials");
        }

        final UserCredentialTable credential = this.getCredential(userId, CredentialType.PASSWORD);
        if (credential == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No credentials");
        }

        final PasswordCredential passwordCredential = credential.getCredential().get(PasswordCredential.class);
        if (passwordCredential == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Malformed credentials");
        }

        if (!this.passwordEncoder.matches(password, passwordCredential.hashedPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Bad credentials");
        }
    }

    public void verifyTotp(final long userId, final String code) {
        if (!StringUtils.hasText(code)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Bad credentials");
        }

        final UserCredentialTable credential = this.getCredential(userId, CredentialType.TOTP);
        if (credential == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No credentials");
        }

        final TotpCredential totpCredential = credential.getCredential().get(TotpCredential.class);
        if (totpCredential == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Malformed credentials");
        }

        final String totpUrl = totpCredential.totpUrl();
        final UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(totpUrl);
        final String secret = builder.build().getQueryParams().getFirst("secret");
        if (!this.codeVerifier.isValidCode(secret, code)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Bad credentials");
        }
    }

    public void verifyBackupCode(final long userId, final String code) {
        if (!StringUtils.hasText(code)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Bad credentials");
        }

        final UserCredentialTable credential = this.getCredential(userId, CredentialType.BACKUP_CODES);
        if (credential == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No credentials");
        }

        final BackupCodeCredential backupCodeCredential = credential.getCredential().get(BackupCodeCredential.class);
        if (backupCodeCredential == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Malformed credentials");
        }

        if (!backupCodeCredential.matches(code)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Bad credentials");
        }

        // TODO mark as used?
    }

    public void checkRemoveBackupCodes() {
        final List<CredentialType> credentialTypes = this.getCredentialTypes(this.getHangarPrincipal().getUserId());
        if (credentialTypes.size() == 1 && credentialTypes.get(0) == CredentialType.BACKUP_CODES) {
            this.removeCredential(this.getHangarPrincipal().getUserId(), CredentialType.BACKUP_CODES);
        }
    }

    public int getAal(final UserTable userTable) {
        final List<CredentialType> types = this.getCredentialTypes(userTable.getUserId());
        if (types.isEmpty() || (types.size() == 1 && types.get(0) == CredentialType.BACKUP_CODES)) {
            return userTable.isEmailVerified() ? 1 : 0;
        } else {
            return 2;
        }
    }
}
