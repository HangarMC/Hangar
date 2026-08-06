package io.papermc.hangar.components.auth.service;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.components.auth.dao.AccountDeletionDAO;
import io.papermc.hangar.components.auth.model.AccountDeletionStatus;
import io.papermc.hangar.components.auth.model.PendingAccountDeletion;
import io.papermc.hangar.components.images.service.AvatarService;
import io.papermc.hangar.components.index.IndexService;
import io.papermc.hangar.db.dao.internal.table.UserDAO;
import io.papermc.hangar.db.dao.internal.table.projects.ProjectsDAO;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.model.db.projects.ProjectTable;
import io.papermc.hangar.service.internal.MailService;
import io.papermc.hangar.service.internal.file.FileService;
import io.papermc.hangar.service.internal.uploads.ProjectFiles;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class AccountDeletionService extends HangarComponent {

    public static final Duration DELETION_DELAY = Duration.ofDays(7);

    private final AccountDeletionDAO accountDeletionDAO;
    private final UserDAO userDAO;
    private final ProjectsDAO projectsDAO;
    private final AvatarService avatarService;
    private final IndexService indexService;
    private final FileService fileService;
    private final ProjectFiles projectFiles;
    private final MailService mailService;
    private final TransactionTemplate transactionTemplate;

    public AccountDeletionService(final AccountDeletionDAO accountDeletionDAO, final UserDAO userDAO, final ProjectsDAO projectsDAO, final AvatarService avatarService, final IndexService indexService, final FileService fileService, final ProjectFiles projectFiles, @Lazy final MailService mailService, final TransactionTemplate transactionTemplate) {
        this.accountDeletionDAO = accountDeletionDAO;
        this.userDAO = userDAO;
        this.projectsDAO = projectsDAO;
        this.avatarService = avatarService;
        this.indexService = indexService;
        this.fileService = fileService;
        this.projectFiles = projectFiles;
        this.mailService = mailService;
        this.transactionTemplate = transactionTemplate;
    }

    public AccountDeletionStatus getStatus(final long userId) {
        return this.accountDeletionDAO.getStatus(userId);
    }

    @Transactional
    public void requestDeletion(final UserTable user, final String confirmation) {
        this.requireCurrentUser(user);
        if (!user.getName().equals(confirmation)) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "auth.settings.account.deletion.confirmationMismatch");
        }

        final AccountDeletionStatus status = this.getStatus(user.getUserId());
        if (status.ownedOrganizationCount() > 0) {
            throw new HangarApiException(HttpStatus.CONFLICT, "auth.settings.account.deletion.ownsOrganizations");
        }
        final OffsetDateTime requestedAt = this.accountDeletionDAO.requestDeletion(user.getUserId());
        if (requestedAt == null) {
            throw new HangarApiException(HttpStatus.CONFLICT, "auth.settings.account.deletion.alreadyRequested");
        }

        final OffsetDateTime deletionDate = requestedAt.plus(DELETION_DELAY);
        this.mailService.queueMail(MailService.MailType.ACCOUNT_DELETION_REQUESTED, user.getEmail(), Map.of(
            "user", user.getName(),
            "date", deletionDate.format(DateTimeFormatter.RFC_1123_DATE_TIME),
            "link", this.config.baseUrl() + "/auth/settings/account"
        ));
    }

    @Transactional
    public void cancelDeletion(final UserTable user) {
        this.requireCurrentUser(user);
        if (this.accountDeletionDAO.cancelDeletion(user.getUserId()) == 0) {
            throw new HangarApiException(HttpStatus.CONFLICT, "auth.settings.account.deletion.notRequested");
        }
        this.mailService.queueMail(MailService.MailType.ACCOUNT_DELETION_CANCELLED, user.getEmail(), Map.of("user", user.getName()));
    }

    private void requireCurrentUser(final UserTable user) {
        if (user.getUserId() != this.getHangarPrincipal().getUserId()) {
            throw HangarApiException.forbidden();
        }
    }

    public void deleteDueAccounts() {
        final OffsetDateTime cutoff = OffsetDateTime.now().minus(DELETION_DELAY);
        for (final long accountId : this.accountDeletionDAO.getDueAccountIds(cutoff)) {
            try {
                this.transactionTemplate.executeWithoutResult(status -> {
                    final PendingAccountDeletion account = this.accountDeletionDAO.getDueAccount(accountId, cutoff);
                    if (account != null) {
                        this.deleteAccount(account);
                    }
                });
            } catch (final Exception ex) {
                this.logger.error("Failed to delete account {}", accountId, ex);
            }
        }
    }

    private void deleteAccount(final PendingAccountDeletion account) {
        final AccountDeletionStatus status = this.getStatus(account.id());
        if (status == null) {
            return;
        }
        if (status.ownedOrganizationCount() > 0) {
            this.accountDeletionDAO.cancelDeletion(account.id());
            this.mailService.queueMail(MailService.MailType.ACCOUNT_DELETION_CANCELLED, account.email(), Map.of("user", account.name()));
            this.logger.warn("Cancelled deletion of account {} because it owns an organization", account.name());
            return;
        }

        for (final ProjectTable project : this.projectsDAO.getUserProjects(account.id(), true)) {
            this.projectsDAO.delete(project);
            this.indexService.removeProject(project.getId());
            this.fileService.deleteDirectory(this.projectFiles.getProjectDir(project.getOwnerName(), project.getSlug()));
            this.avatarService.deleteProjectAvatar(project.getId());
        }
        this.fileService.deleteDirectory(this.projectFiles.getTempDir(account.name()));
        this.avatarService.deleteUserAvatar(account.uuid());
        this.userDAO.delete(account.id());
        this.mailService.queueMail(MailService.MailType.ACCOUNT_DELETED, account.email(), Map.of("user", account.name()));
    }
}
