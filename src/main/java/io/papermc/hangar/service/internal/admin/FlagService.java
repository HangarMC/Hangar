package io.papermc.hangar.service.internal.admin;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.db.dao.internal.projects.HangarProjectFlagsDAO;
import io.papermc.hangar.db.dao.internal.table.projects.ProjectFlagNotificationsDAO;
import io.papermc.hangar.db.dao.internal.table.projects.ProjectFlagsDAO;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.common.projects.FlagReason;
import io.papermc.hangar.model.db.projects.ProjectFlagNotificationTable;
import io.papermc.hangar.model.db.projects.ProjectFlagTable;
import io.papermc.hangar.model.internal.logs.LogAction;
import io.papermc.hangar.model.internal.logs.contexts.ProjectContext;
import io.papermc.hangar.model.internal.projects.HangarProjectFlag;
import io.papermc.hangar.model.internal.user.notifications.NotificationType;
import io.papermc.hangar.service.internal.users.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FlagService extends HangarComponent {

    private final ProjectFlagsDAO projectFlagsDAO;
    private final HangarProjectFlagsDAO hangarProjectFlagsDAO;
    private final ProjectFlagNotificationsDAO flagNotificationsDAO;
    private final NotificationService notificationService;

    @Autowired
    public FlagService(ProjectFlagsDAO projectFlagsDAO, HangarProjectFlagsDAO hangarProjectFlagsDAO, final ProjectFlagNotificationsDAO flagNotificationsDAO, final NotificationService notificationService) {
        this.projectFlagsDAO = projectFlagsDAO;
        this.hangarProjectFlagsDAO = hangarProjectFlagsDAO;
        this.flagNotificationsDAO = flagNotificationsDAO;
        this.notificationService = notificationService;
    }

    public void createFlag(long projectId, FlagReason reason, String comment) {
        if (hasUnresolvedFlag(projectId, getHangarPrincipal().getId())) {
            throw new HangarApiException("project.flag.error.alreadyOpen");
        }
        projectFlagsDAO.insert(new ProjectFlagTable(projectId, getHangarPrincipal().getId(), reason, comment));
        actionLogger.project(LogAction.PROJECT_FLAGGED.create(ProjectContext.of(projectId), "Flagged by " + getHangarPrincipal().getName(), ""));
    }

    public boolean hasUnresolvedFlag(long projectId, long userId) {
        return projectFlagsDAO.getUnresolvedFlag(projectId, userId) != null;
    }

    public void markAsResolved(long flagId, boolean resolved) {
        HangarProjectFlag hangarProjectFlag = hangarProjectFlagsDAO.getById(flagId);
        if (hangarProjectFlag == null) {
            throw new HangarApiException(HttpStatus.NOT_FOUND);
        }
        if (hangarProjectFlag.isResolved()) {
            throw new HangarApiException("project.flag.error.alreadyResolved");
        }
        Long resolvedBy = resolved ? getHangarPrincipal().getId() : null;
        OffsetDateTime resolvedAt = resolved ? OffsetDateTime.now() : null;
        projectFlagsDAO.markAsResolved(flagId, resolved, resolvedBy, resolvedAt);
        hangarProjectFlag.logAction(actionLogger, LogAction.PROJECT_FLAG_RESOLVED, "Flag resolved by " + getHangarPrincipal().getName(), "Flag reported by " + hangarProjectFlag.getReportedByName());
    }

    public void notifyParty(final long flagId, final boolean warning, final boolean toReporter, final String notification) {
        final HangarProjectFlag flag = hangarProjectFlagsDAO.getById(flagId);
        final String[] message = {"notifications.project.manualReportComment", flag.getProjectNamespace().getSlug(), notification};
        if (toReporter) {
            final long id = notificationService.notify(flag.getUserId(), null, flag.getProjectId(), warning ? NotificationType.WARNING : NotificationType.INFO, message).getId();
            flagNotificationsDAO.insert(new ProjectFlagNotificationTable(flag.getId(), id, getHangarUserId()));
        } else {
            final List<ProjectFlagNotificationTable> tables = notificationService.notifyProjectMembers(flag.getProjectId(), flag.getProjectNamespace().getOwner(),
                    flag.getProjectNamespace().getSlug(), warning ? NotificationType.WARNING : NotificationType.INFO, message).stream()
                .map(table -> new ProjectFlagNotificationTable(flag.getId(), table.getId(), getHangarUserId())).collect(Collectors.toList());
            flagNotificationsDAO.insert(tables);
        }
    }

    public List<HangarProjectFlag> getFlags(long projectId) {
        return hangarProjectFlagsDAO.getFlags(projectId);
    }

    public List<HangarProjectFlag> getFlags() {
        return hangarProjectFlagsDAO.getFlags();
    }
}
