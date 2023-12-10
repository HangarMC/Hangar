package io.papermc.hangar.components.auth.service;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.components.auth.dao.UserCredentialDAO;
import io.papermc.hangar.components.auth.model.credential.CredentialType;
import io.papermc.hangar.components.auth.model.credential.PasswordCredential;
import io.papermc.hangar.components.auth.model.db.UserCredentialTable;
import io.papermc.hangar.components.auth.model.dto.SignupForm;
import io.papermc.hangar.components.auth.model.dto.login.LoginResponse;
import io.papermc.hangar.db.customtypes.JSONB;
import io.papermc.hangar.db.dao.internal.table.UserDAO;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.api.UserNameChange;
import io.papermc.hangar.model.common.Permission;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.model.internal.user.HangarUser;
import io.papermc.hangar.security.authentication.HangarPrincipal;
import io.papermc.hangar.service.ValidationService;
import io.papermc.hangar.service.api.UsersApiService;
import io.papermc.hangar.service.internal.MailService;
import jakarta.validation.constraints.NotEmpty;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AuthService extends HangarComponent implements UserDetailsService {

    private final UserDAO userDAO;
    private final UserCredentialDAO userCredentialDAO;
    private final PasswordEncoder passwordEncoder;
    private final ValidationService validationService;
    private final VerificationService verificationService;
    private final CredentialsService credentialsService;
    private final HibpService hibpService;
    private final MailService mailService;
    private final TokenService tokenService;
    private final UsersApiService usersApiService;

    public AuthService(final UserDAO userDAO, final UserCredentialDAO userCredentialDAO, final PasswordEncoder passwordEncoder, final ValidationService validationService, final VerificationService verificationService, final CredentialsService credentialsService, final HibpService hibpService, final MailService mailService, final TokenService tokenService, final UsersApiService usersApiService) {
        this.userDAO = userDAO;
        this.userCredentialDAO = userCredentialDAO;
        this.passwordEncoder = passwordEncoder;
        this.validationService = validationService;
        this.verificationService = verificationService;
        this.credentialsService = credentialsService;
        this.hibpService = hibpService;
        this.mailService = mailService;
        this.tokenService = tokenService;
        this.usersApiService = usersApiService;
    }

    @Transactional
    public UserTable registerUser(final SignupForm form) {
        if (!this.validPassword(form.password(), form.username())) {
            throw new HangarApiException("dum");
        }
        this.validateNewUser(form.username(), form.email(), form.tos());

        final UserTable userTable = this.userDAO.create(UUID.randomUUID(), form.username(), form.email(), null, "en", List.of(), false, "light", false, new JSONB(Map.of()));
        this.credentialsService.registerCredential(userTable.getUserId(), new PasswordCredential(this.passwordEncoder.encode(form.password())));
        this.verificationService.sendVerificationCode(userTable.getUserId(), userTable.getEmail(), userTable.getName());

        return userTable;
    }

    public void validateNewUser(final String username, final String email, final boolean tos) {
        if (!tos) {
            throw new HangarApiException("nav.user.error.tos");
        }
        if (!this.validationService.isValidUsername(username)) {
            throw new HangarApiException("nav.user.error.invalidUsername");
        }
        if (this.userDAO.getUserTable(username) != null) {
            throw new HangarApiException("nav.user.error.usernameTaken");
        }
        if (this.userDAO.getUserTable(email) != null) {
            throw new HangarApiException("nav.user.error.emailTaken");
        }
    }

    public boolean validPassword(final String password, final String username) {
        if (!StringUtils.hasText(password) || password.length() < 8) {
            throw new HangarApiException("The password needs to be at least 8 characters long");
        }

        // https://github.com/ory/kratos/blob/40ab76af4f36c671fc1d1108c3b6a15adcdb6125/selfservice/strategy/password/validator.go#L185
        final String lowerPass = password.toLowerCase();
        final String lowerUser = username.toLowerCase();
        //noinspection deprecation
        final int dist = org.apache.commons.lang3.StringUtils.getLevenshteinDistance(lowerUser, lowerPass);
        final float lcs = ((float) io.papermc.hangar.util.StringUtils.lcs(lowerUser, lowerPass).length()) / lowerPass.length();
        if (dist < 5 || lcs > 0.5) {
            throw new HangarApiException("The username and password are too similar");
        }

        try {
            final int breachAmount = this.hibpService.getBreachAmount(password);
            if (breachAmount > 10) {
                throw new HangarApiException("Your password has been found in data breaches, please use a different one");
            }
        } catch (final Exception e) {
            // Don't block account creation if this isn't reachable
            e.printStackTrace();
        }

        return true;
    }

    @Override
    public HangarPrincipal loadUserByUsername(final String username) throws UsernameNotFoundException {
        if (username == null) {
            throw new UsernameNotFoundException("no user with null username");
        }
        final UserTable userTable = this.userDAO.getUserTable(username);
        if (userTable == null) {
            throw new UsernameNotFoundException("no user in table");
        }
        final UserCredentialTable passwordCredential = this.userCredentialDAO.getByType(CredentialType.PASSWORD, userTable.getUserId());
        if (passwordCredential == null) {
            throw new UsernameNotFoundException("no password credentials in table");
        }
        final String password = passwordCredential.getCredential().get(PasswordCredential.class).hashedPassword();
        final int aal = this.credentialsService.getAal(userTable);
        // TODO load proper perms
        // when exactly is this called? do we even care about perms? do we care about privileged?
        return new HangarPrincipal(userTable.getUserId(), userTable.getName(), userTable.getEmail(), userTable.isLocked(), Permission.ViewPublicInfo, password, aal, false);
    }

    @Transactional
    public void handleUsernameChange(final UserTable user, final String newName) {
        // need to be a valid name
        if (!this.validationService.isValidUsername(newName)) {
            throw new HangarApiException("nav.user.error.invalidUsername");
        }

        // make sure a user with that name doesn't exist yet
        if (this.userDAO.getUserTable(newName) != null && !user.getName().equalsIgnoreCase(newName)) {
            throw new HangarApiException("That username is unavailable");
        }
        // check that last change was long ago
        final List<UserNameChange> userNameHistory = this.userDAO.getUserNameHistory(user.getUuid());
        if (!userNameHistory.isEmpty()) {
            userNameHistory.sort(Comparator.comparing(UserNameChange::date).reversed());
            final OffsetDateTime nextChange = userNameHistory.get(0).date().plusDays(this.config.user.nameChangeInterval());
            if (nextChange.isAfter(OffsetDateTime.now())) {
                throw new HangarApiException("You have to wait until " + nextChange.format(DateTimeFormatter.RFC_1123_DATE_TIME) + " before being able to change your username again");
            }
        }
        // do the change
        final String oldName = user.getName();
        user.setName(newName);
        this.userDAO.update(user);
        // record the change into the db
        this.userDAO.recordNameChange(user.getUuid(), oldName, newName);
        // email
        this.mailService.queueMail(MailService.MailType.USERNAME_CHANGED, user.getEmail(), Map.of("oldName", oldName, "newName", newName));
    }

    @Transactional
    public void handleEmailChange(final UserTable userTable, final @NotEmpty String email) {
        // make sure a user with that email doesn't exist yet
        if (this.userDAO.getUserTable(email) != null) {
            throw new HangarApiException("You can't use this email!");
        }

        // send info mail
        this.mailService.queueMail(MailService.MailType.EMAIL_CHANGED, userTable.getEmail(), Map.of("user", userTable.getName(), "newMail", email));
        // update
        userTable.setEmail(email);
        userTable.setEmailVerified(false);
        this.userDAO.update(userTable);
        // send verification mail
        this.verificationService.sendVerificationCode(userTable.getUserId(), userTable.getEmail(), userTable.getName());
    }

    @Transactional
    public void handlePasswordChange(final UserTable userTable, final String newPassword) {
        if (!this.validPassword(newPassword, userTable.getName())) {
            return;
        }
        // update
        this.credentialsService.removeCredential(userTable.getUserId(), CredentialType.PASSWORD);
        this.credentialsService.registerCredential(userTable.getUserId(), new PasswordCredential(this.passwordEncoder.encode(newPassword)));
        // send info mail
        this.mailService.queueMail(MailService.MailType.PASSWORD_CHANGED, userTable.getEmail(), Map.of("user", userTable.getName()));
    }

    public LoginResponse setAalAndLogin(final UserTable userTable, final int aal) {
        // todo create session

        // issue refresh token
        this.tokenService.issueRefreshToken(userTable.getUserId(), this.response);
        // issue access token
        final String token = this.tokenService.newPrivilegedToken(userTable);
        // get user
        final HangarUser user = this.usersApiService.getUser(userTable.getName(), HangarUser.class);
        user.setAccessToken(token);
        user.setAal(aal);
        // return
        return new LoginResponse(aal, null, user);
    }
}
