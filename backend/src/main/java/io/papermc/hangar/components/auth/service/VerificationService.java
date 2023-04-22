package io.papermc.hangar.components.auth.service;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.components.auth.dao.VerificationCodeDao;
import io.papermc.hangar.components.auth.model.db.VerificationCodeTable;
import io.papermc.hangar.db.dao.internal.table.UserDAO;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.service.internal.MailService;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;

@Service
public class VerificationService extends HangarComponent {

    private final SecureRandom secureRandom = new SecureRandom();
    private final UserDAO userDAO;
    private final VerificationCodeDao verificationCodeDao;
    private final MailService mailService;

    public VerificationService(final UserDAO userDAO, final VerificationCodeDao verificationCodeDao, final MailService mailService) {
        this.userDAO = userDAO;
        this.verificationCodeDao = verificationCodeDao;
        this.mailService = mailService;
    }

    public @Nullable VerificationCodeTable getVerificationCode(final long userId, final VerificationCodeTable.VerificationCodeType type) {
        return this.verificationCodeDao.get(type, userId);
    }

    public boolean expired(final VerificationCodeTable table) {
        return table.getCreatedAt().plus(10, ChronoUnit.MINUTES).isBefore(OffsetDateTime.now());
    }

    public boolean verifyResetCode(final String email, final String code, final boolean delete) {
        final UserTable userTable = this.userDAO.getUserTable(email);
        if (userTable == null) {
            throw new HangarApiException("Invalid email");
        }

        final VerificationCodeTable table = this.verificationCodeDao.get(VerificationCodeTable.VerificationCodeType.PASSWORD_RESET, userTable.getUserId());
        if (table == null) {
            throw new HangarApiException("Invalid verification code");
        }
        if (this.expired(table)) {
            throw new HangarApiException("The verification code expired, please request a new one");
        }

        if (!table.getCode().equals(code)) {
            throw new HangarApiException("Invalid verification code");
        }

        if (delete) {
            this.verificationCodeDao.delete(table.getId());
        }

        return true;
    }

    public void sendResetCode(final String email) {
        final UserTable userTable = this.userDAO.getUserTable(email);
        if (userTable == null) {
            return;
        }

        this.verificationCodeDao.deleteOld(VerificationCodeTable.VerificationCodeType.PASSWORD_RESET, userTable.getUserId());

        final String code = String.format("%06d", this.secureRandom.nextInt(999999));
        this.verificationCodeDao.insert(new VerificationCodeTable(userTable.getUserId(), VerificationCodeTable.VerificationCodeType.PASSWORD_RESET, code));

        this.mailService.queueMail(MailService.MailType.PASSWORD_RESET, userTable.getEmail(), Map.of("user", userTable.getName(), "code", code));
    }

    public void sendVerificationCode(final long userId, final String email, final String name) {
        this.verificationCodeDao.deleteOld(VerificationCodeTable.VerificationCodeType.EMAIL_VERIFICATION, userId);

        final String code = String.format("%06d", this.secureRandom.nextInt(999999));
        this.verificationCodeDao.insert(new VerificationCodeTable(userId, VerificationCodeTable.VerificationCodeType.EMAIL_VERIFICATION, code));

        final String link = this.config.getBaseUrl() + "/auth/settings/account?verify=" + code;

        this.mailService.queueMail(MailService.MailType.EMAIL_CONFIRMATION, email, Map.of("user", name, "code", code, "link", link));
    }

    public boolean verifyEmailCode(final long id, final String code) {
        final UserTable userTable = this.userDAO.getUserTable(id);
        if (userTable == null) {
            return false;
        }

        final VerificationCodeTable table = this.verificationCodeDao.get(VerificationCodeTable.VerificationCodeType.EMAIL_VERIFICATION, userTable.getUserId());
        if (table == null) {
            return false;
        }
        if (this.expired(table)) {
            return false; // TODO expired
        }
        if (!table.getCode().equals(code)) {
            return false;
        }

        userTable.setEmailVerified(true);
        this.userDAO.update(userTable);

        this.verificationCodeDao.delete(table.getId());

        return true;
    }
}
