package io.papermc.hangar.service.internal.admin;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.components.images.service.AvatarService;
import io.papermc.hangar.components.webhook.model.event.ProjectFlaggedEvent;
import io.papermc.hangar.components.webhook.service.WebhookService;
import io.papermc.hangar.db.dao.internal.projects.HangarProjectFlagNofiticationsDAO;
import io.papermc.hangar.db.dao.internal.projects.HangarProjectFlagsDAO;
import io.papermc.hangar.db.dao.internal.table.projects.ProjectFlagNotificationsDAO;
import io.papermc.hangar.db.dao.internal.table.projects.ProjectFlagsDAO;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.api.PaginatedResult;
import io.papermc.hangar.model.api.Pagination;
import io.papermc.hangar.model.api.requests.RequestPagination;
import io.papermc.hangar.model.common.projects.FlagReason;
import io.papermc.hangar.model.db.projects.ProjectFlagNotificationTable;
import io.papermc.hangar.model.db.projects.ProjectFlagTable;
import io.papermc.hangar.model.db.projects.ProjectTable;
import io.papermc.hangar.model.internal.logs.LogAction;
import io.papermc.hangar.model.internal.logs.contexts.ProjectContext;
import io.papermc.hangar.model.internal.projects.HangarProjectFlag;
import io.papermc.hangar.model.internal.projects.HangarProjectFlagNotification;
import io.papermc.hangar.model.internal.user.notifications.NotificationType;
import io.papermc.hangar.service.internal.projects.ProjectService;
import io.papermc.hangar.service.internal.users.NotificationService;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FlagService extends HangarComponent {

    private final ProjectFlagsDAO projectFlagsDAO;
    private final HangarProjectFlagsDAO hangarProjectFlagsDAO;
    private final ProjectFlagNotificationsDAO flagNotificationsDAO;
    private final HangarProjectFlagNofiticationsDAO hangarProjectFlagNofiticationsDAO;
    private final NotificationService notificationService;
    private final WebhookService webhookService;
    private final ProjectService projectService;
    private final AvatarService avatarService;

    @Autowired
    public FlagService(final ProjectFlagsDAO projectFlagsDAO, final HangarProjectFlagsDAO hangarProjectFlagsDAO, final ProjectFlagNotificationsDAO flagNotificationsDAO, final HangarProjectFlagNofiticationsDAO hangarProjectFlagNofiticationsDAO, final NotificationService notificationService, final WebhookService webhookService, final ProjectService projectService, final AvatarService avatarService) {
        this.projectFlagsDAO = projectFlagsDAO;
        this.hangarProjectFlagsDAO = hangarProjectFlagsDAO;
        this.flagNotificationsDAO = flagNotificationsDAO;
        this.hangarProjectFlagNofiticationsDAO = hangarProjectFlagNofiticationsDAO;
        this.notificationService = notificationService;
        this.webhookService = webhookService;
        this.projectService = projectService;
        this.avatarService = avatarService;
    }

    @Transactional
    public void createFlag(final long projectId, final FlagReason reason, final String comment) {
        if (!(this.projectService.getProjectTable(projectId) instanceof ProjectTable projectTable)) {
            throw new HangarApiException(HttpStatus.NOT_FOUND, "Project table not found");
        }
        if (this.hasUnresolvedFlag(projectId, this.getHangarPrincipal().getId())) {
            throw new HangarApiException("project.flag.error.alreadyOpen");
        }
        if (comment.length() > 500) {
            throw new HangarApiException("project.flag.error.commentTooLong");
        }
        this.projectFlagsDAO.insert(new ProjectFlagTable(projectId, this.getHangarPrincipal().getId(), reason, comment));
        this.actionLogger.project(LogAction.PROJECT_FLAGGED.create(ProjectContext.of(projectId), "Flagged by " + this.getHangarPrincipal().getName(), ""));
        // TODO rewrite avatar fetching (for move this code to an async method)
        final String avatarUrl = this.avatarService.getProjectAvatarUrl(projectTable.getProjectId(), projectTable.getOwnerName());
        this.webhookService.handleEvent(new ProjectFlaggedEvent(this.getHangarPrincipal().getName(),
            this.config.baseUrl() + "/" + this.getHangarPrincipal().getName(),
            reason.name().toLowerCase(Locale.ROOT),
            comment,
            projectTable.getOwnerName(),
            projectTable.getName(),
            avatarUrl,
            this.config.baseUrl() + "/" + projectTable.getOwnerName() + "/" + projectTable.getName(),
            List.of(),
            this.config.baseUrl() + "/admin/flags"
        ));
    }

    public boolean hasUnresolvedFlag(final long projectId, final long userId) {
        return this.projectFlagsDAO.getUnresolvedFlag(projectId, userId) != null;
    }

    @Transactional
    public void markAsResolved(final long flagId, final boolean resolved) {
        final HangarProjectFlag hangarProjectFlag = this.hangarProjectFlagsDAO.getById(flagId);
        if (hangarProjectFlag == null) {
            throw new HangarApiException(HttpStatus.NOT_FOUND);
        }
        if (resolved == hangarProjectFlag.isResolved()) {
            throw new HangarApiException("project.flag.error.alreadyResolved");
        }
        final Long resolvedBy = resolved ? this.getHangarPrincipal().getId() : null;
        final OffsetDateTime resolvedAt = resolved ? OffsetDateTime.now() : null;
        this.projectFlagsDAO.markAsResolved(flagId, resolved, resolvedBy, resolvedAt);
        hangarProjectFlag.logAction(this.actionLogger, LogAction.PROJECT_FLAG_RESOLVED, "Flag resolved by " + this.getHangarPrincipal().getName(), "Flag reported by " + hangarProjectFlag.getReportedByName());
    }

    @Transactional
    public void notifyParty(final long flagId, final boolean warning, final boolean toReporter, final String notification) {
        final HangarProjectFlag flag = this.hangarProjectFlagsDAO.getById(flagId);
        final String[] message = {"notifications.project.manualReportComment", flag.getProjectNamespace().getSlug(), notification};
        if (toReporter) {
            final long id = this.notificationService.notify(flag.getUserId(), null, this.getHangarUserId(), warning ? NotificationType.WARNING : NotificationType.INFO, message).getId();
            this.flagNotificationsDAO.insert(new ProjectFlagNotificationTable(flag.getId(), id));
        } else {
            final List<ProjectFlagNotificationTable> tables = this.notificationService.notifyProjectMembers(flag.getProjectId(), this.getHangarUserId(), flag.getProjectNamespace().getOwner(),
                    flag.getProjectNamespace().getSlug(), warning ? NotificationType.WARNING : NotificationType.INFO, message).stream()
                .map(table -> new ProjectFlagNotificationTable(flag.getId(), table.getId())).collect(Collectors.toList());
            this.flagNotificationsDAO.insert(tables);
        }
    }

    public List<HangarProjectFlagNotification> getFlagNotifications(final long flagId) {
        return this.hangarProjectFlagNofiticationsDAO.getNotifications(flagId);
    }

    public List<HangarProjectFlag> getFlags(final String slug) {
        return this.hangarProjectFlagsDAO.getFlags(slug);
    }

    @Transactional(readOnly = true)
    public PaginatedResult<HangarProjectFlag> getFlags(final RequestPagination pagination, final boolean resolved) {
        final List<HangarProjectFlag> flags = this.hangarProjectFlagsDAO.getFlags(pagination, resolved);
        final long count = this.hangarProjectFlagsDAO.getFlagsCount(resolved);
        return new PaginatedResult<>(new Pagination(count, pagination), flags);
    }

    public long getFlagsQueueSize(final boolean resolved) {
        return this.hangarProjectFlagsDAO.getFlagsCount(resolved);
    }
}
