package io.papermc.hangar.components.auth.service;

import io.papermc.hangar.components.auth.dao.VerificationCodeDao;
import io.papermc.hangar.components.auth.model.db.VerificationCodeTable;
import io.papermc.hangar.db.dao.internal.table.UserDAO;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.service.internal.MailService;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;

@Service
public class VerificationService {

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

    public boolean verifyResetCode(final String email, final String code, final boolean delete) {
        final UserTable userTable = this.userDAO.getUserTable(email);
        if (userTable == null) {
            return false;
        }

        final VerificationCodeTable table = this.verificationCodeDao.get(VerificationCodeTable.VerificationCodeType.PASSWORD_RESET, userTable.getUserId());
        if (table == null) {
            return false;
        }
        if (table.getCreatedAt().plus(10, ChronoUnit.MINUTES).isBefore(OffsetDateTime.now())) {
            return false; // TODO expired
        }

        if (!table.getCode().equals(code)) {
            return false;
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

        this.mailService.queueEmail("Hangar Password Reset", userTable.getEmail(), """
            Hi %s,
            you dum dum did forget your password, enter %s to reset.
            if you did not request this email, ignore it.
            """.formatted(userTable.getName(), code));
    }

    public void sendVerificationCode(final long userId, final String email, final String name) {
        this.verificationCodeDao.deleteOld(VerificationCodeTable.VerificationCodeType.EMAIL_VERIFICATION, userId);

        final String code = String.format("%06d", this.secureRandom.nextInt(999999));
        this.verificationCodeDao.insert(new VerificationCodeTable(userId, VerificationCodeTable.VerificationCodeType.EMAIL_VERIFICATION, code));

        this.mailService.queueEmail("Hangar email verification", email, """
            Hi %s,
            here is ya email verification code: %s
            """.formatted(name, code));
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
        if (table.getCreatedAt().plus(10, ChronoUnit.MINUTES).isBefore(OffsetDateTime.now())) {
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
