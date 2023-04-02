package io.papermc.hangar.components.auth.service;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.components.auth.dao.UserCredentialDAO;
import io.papermc.hangar.components.auth.model.credential.CredentialType;
import io.papermc.hangar.components.auth.model.credential.PasswordCredential;
import io.papermc.hangar.components.auth.model.db.UserCredentialTable;
import io.papermc.hangar.db.customtypes.JSONB;
import io.papermc.hangar.db.dao.internal.table.UserDAO;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.exceptions.WebHookException;
import io.papermc.hangar.model.api.UserNameChange;
import io.papermc.hangar.model.common.Permission;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.components.auth.model.dto.SignupForm;
import io.papermc.hangar.security.authentication.HangarPrincipal;
import io.papermc.hangar.service.ValidationService;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
    private final ValidationService validationService;
    private final VerificationService verificationService;
    private final CredentialsService credentialsService;
    private final HibpService hibpService;

    public AuthService(final UserDAO userDAO, final UserCredentialDAO userCredentialDAO, final PasswordEncoder passwordEncoder, final ValidationService validationService, final VerificationService verificationService, final CredentialsService credentialsService, final HibpService hibpService) {
        this.userDAO = userDAO;
        this.userCredentialDAO = userCredentialDAO;
        this.passwordEncoder = passwordEncoder;
        this.validationService = validationService;
        this.verificationService = verificationService;
        this.credentialsService = credentialsService;
        this.hibpService = hibpService;
    }

    @Transactional
    public UserTable registerUser(final SignupForm form) {
        if (!this.validationService.isValidUsername(form.username())) {
            throw new HangarApiException("nav.user.error.invalidUsername");
        }
        if (!this.validPassword(form.password())) {
            throw new HangarApiException("dum");
        }
        // TODO check if user exists and shit
        final UserTable userTable = this.userDAO.create(UUID.randomUUID(), form.username(), form.email(), null, "en", List.of(), false, "light", false, new JSONB(Map.of()));
        this.credentialsService.registerCredential(userTable.getUserId(), new PasswordCredential(this.passwordEncoder.encode(form.password())));
        this.verificationService.sendVerificationCode(userTable.getUserId(), userTable.getEmail(), userTable.getName());

        return userTable;
    }

    public boolean validPassword(final String password) {
        // TODO validate password against pw rules

        final int breachAmount = this.hibpService.getBreachAmount(password);
        if (breachAmount > 0) {
            throw new HangarApiException("ur password sucks and was leaked " + breachAmount + " times, use something better!");
        }
        return true;
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
        return new HangarPrincipal(userTable.getUserId(), userTable.getName(), userTable.getEmail(), userTable.isLocked(), Permission.ViewPublicInfo, password, userTable.isEmailVerified());
    }

    // TODO call this
    private void handleUsernameChange(final UserTable user, final String newName) {
        // make sure a user with that name doesn't exist yet
        if (this.userDAO.getUserTable(newName) != null) {
            throw new HangarApiException("A user with that name already exists!");
        }
        // check that last change was long ago
        final List<UserNameChange> userNameHistory = this.userDAO.getUserNameHistory(user.getUuid());
        if (!userNameHistory.isEmpty()) {
            userNameHistory.sort(Comparator.comparing(UserNameChange::date).reversed());
            final OffsetDateTime nextChange = userNameHistory.get(0).date().plus(this.config.user.nameChangeInterval(), ChronoUnit.DAYS);
            if (nextChange.isAfter(OffsetDateTime.now())) {
                throw WebHookException.of("You can't change your name that soon! You have to wait till " + nextChange.format(DateTimeFormatter.RFC_1123_DATE_TIME));
            }
        }
        // record the change into the db
        this.userDAO.recordNameChange(user.getUuid(), user.getName(), newName);
    }

    // TODO call this
    private void handleEmailChange() {
        // todo set email to unconfirmed and send email
    }
}
