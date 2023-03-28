package io.papermc.hangar.components.auth.service;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.components.auth.dao.UserCredentialDAO;
import io.papermc.hangar.components.auth.model.credential.BackupCodeCredential;
import io.papermc.hangar.components.auth.model.credential.Credential;
import io.papermc.hangar.components.auth.model.credential.CredentialType;
import io.papermc.hangar.components.auth.model.credential.PasswordCredential;
import io.papermc.hangar.components.auth.model.db.UserCredentialTable;
import io.papermc.hangar.db.customtypes.JSONB;
import io.papermc.hangar.db.dao.internal.table.UserDAO;
import io.papermc.hangar.model.common.Permission;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.components.auth.model.dto.SignupForm;
import io.papermc.hangar.security.authentication.HangarPrincipal;
import java.util.List;
import java.util.UUID;
import org.jetbrains.annotations.Nullable;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService extends HangarComponent implements UserDetailsService {

    private final UserDAO userDAO;
    private final UserCredentialDAO userCredentialDAO;
    private final PasswordEncoder passwordEncoder;

    public AuthService(final UserDAO userDAO, final UserCredentialDAO userCredentialDAO, final PasswordEncoder passwordEncoder) {
        this.userDAO = userDAO;
        this.userCredentialDAO = userCredentialDAO;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserTable registerUser(final SignupForm form) {
        // TODO check if user exists and shit
        final UserTable userTable = this.userDAO.create(UUID.randomUUID(), form.username(), form.email(), null, "en", List.of(), false, "light");

        this.registerCredential(userTable.getUserId(), new PasswordCredential(this.passwordEncoder.encode(form.password())));

        return userTable;
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

    @Override
    public HangarPrincipal loadUserByUsername(final String username) throws UsernameNotFoundException {
        if (username == null) {
            throw new UsernameNotFoundException("no user with null username");
        }
        System.out.println("loading user " + username);
        final UserTable userTable = this.userDAO.getUserTable(username);
        if (userTable == null) {
            throw new UsernameNotFoundException("no user in table");
        }
        final UserCredentialTable passwordCredential = this.userCredentialDAO.getByType(CredentialType.PASSWORD, userTable.getUserId());
        if (passwordCredential == null) {
            throw new UsernameNotFoundException("no password credentials in table");
        }
        final String password = passwordCredential.getCredential().get(PasswordCredential.class).hashedPassword();
        // TODO load proper perms
        return new HangarPrincipal(userTable.getUserId(), userTable.getName(), userTable.isLocked(), Permission.ViewPublicInfo, password);
    }
}
