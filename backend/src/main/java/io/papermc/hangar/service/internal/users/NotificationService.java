package io.papermc.hangar.service.internal.users;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.db.dao.internal.HangarNotificationsDAO;
import io.papermc.hangar.db.dao.internal.projects.HangarProjectsDAO;
import io.papermc.hangar.db.dao.internal.table.NotificationsDAO;
import io.papermc.hangar.db.dao.internal.table.projects.ProjectsDAO;
import io.papermc.hangar.model.api.PaginatedResult;
import io.papermc.hangar.model.api.Pagination;
import io.papermc.hangar.model.api.requests.RequestPagination;
import io.papermc.hangar.model.common.Permission;
import io.papermc.hangar.model.common.projects.Visibility;
import io.papermc.hangar.model.db.NotificationTable;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.model.db.projects.ProjectTable;
import io.papermc.hangar.model.db.roles.ProjectRoleTable;
import io.papermc.hangar.model.db.versions.ProjectVersionTable;
import io.papermc.hangar.model.internal.user.JoinableMember;
import io.papermc.hangar.model.internal.user.notifications.HangarNotification;
import io.papermc.hangar.model.internal.user.notifications.NotificationType;
import io.papermc.hangar.service.PermissionService;
import java.util.ArrayList;
import java.util.List;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService extends HangarComponent {

    private final NotificationsDAO notificationsDAO;
    private final HangarNotificationsDAO hangarNotificationsDAO;
    private final HangarProjectsDAO hangarProjectsDAO;
    private final ProjectsDAO projectsDAO;
    private final PermissionService permissionService;

    public NotificationService(final NotificationsDAO notificationsDAO, final HangarNotificationsDAO hangarNotificationsDAO, final HangarProjectsDAO hangarProjectsDAO, final ProjectsDAO projectsDAO, final PermissionService permissionService) {
        this.notificationsDAO = notificationsDAO;
        this.hangarNotificationsDAO = hangarNotificationsDAO;
        this.hangarProjectsDAO = hangarProjectsDAO;
        this.projectsDAO = projectsDAO;
        this.permissionService = permissionService;
    }

    public List<HangarNotification> getRecentNotifications(final int amount) {
        return this.hangarNotificationsDAO.getNotifications(this.getHangarUserId(), amount);
    }

    @Transactional(readOnly = true)
    public PaginatedResult<HangarNotification> getNotifications(final RequestPagination pagination, final @Nullable Boolean read) {
        final List<HangarNotification> notifications = read != null ? this.hangarNotificationsDAO.getNotifications(this.getHangarUserId(), read, pagination) : this.hangarNotificationsDAO.getNotifications(this.getHangarUserId(), pagination);
        return new PaginatedResult<>(new Pagination(this.getUnreadNotifications(), pagination), notifications);
    }

    public long getUnreadNotifications() {
        return this.notificationsDAO.getUnreadNotificationCount(this.getHangarUserId());
    }

    public boolean markNotificationAsRead(final long notificationId) {
        return this.notificationsDAO.markAsRead(notificationId, this.getHangarUserId());
    }

    public void markNotificationsAsRead() {
        this.notificationsDAO.markAllAsRead(this.getHangarUserId());
    }

    public void notifyVisibilityChange(final ProjectTable projectTable, final Visibility visibility, final @Nullable String comment) {
        final NotificationType notificationType = visibility == Visibility.SOFTDELETE || visibility == Visibility.NEEDSCHANGES
            || visibility == Visibility.NEEDSAPPROVAL ? NotificationType.WARNING : NotificationType.SUCCESS;
        final String[] message = comment != null && !comment.isBlank()
            ? new String[]{"notifications.project.visibilityChangedComment", projectTable.getName(), visibility.name(), comment}
            : new String[]{"notifications.project.visibilityChanged", projectTable.getName(), visibility.name()};
        this.notifyProjectMembers(projectTable.getProjectId(), this.getHangarUserId(), projectTable.getOwnerName(), projectTable.getSlug(), notificationType, message);
    }

    public NotificationTable notify(final long userId, final @Nullable String action, final @Nullable Long originUserId, final NotificationType notificationType, final String[] message) {
        return this.notificationsDAO.insert(new NotificationTable(userId, action, originUserId, message, notificationType));
    }

    public List<NotificationTable> notifyProjectMembers(final long projectId, final @Nullable Long originUserId,
                                                        final String owner, final String slug, final NotificationType notificationType, final String[] message) {
        final List<NotificationTable> notifications = new ArrayList<>();
        final List<JoinableMember<ProjectRoleTable>> members = this.hangarProjectsDAO.getProjectMembers(projectId, null, false);
        for (final JoinableMember<ProjectRoleTable> member : members) {
            notifications.add(new NotificationTable(
                member.getUser().getUserId(),
                owner + "/" + slug,
                originUserId,
                message, notificationType)
            );
        }
        return this.notificationsDAO.insert(notifications);
    }

    public void notifyUsersNewVersion(final ProjectTable projectTable, final ProjectVersionTable projectVersionTable, final List<UserTable> projectWatchers) {
        final List<NotificationTable> notificationTables = new ArrayList<>();
        final String action = projectTable.getOwnerName() + "/" + projectTable.getSlug() + "/versions/" + projectVersionTable.getVersionString();
        final Long userId = this.getHangarUserId();
        final String[] messageArgs = {"notifications.project.newVersion", projectTable.getName(), projectVersionTable.getVersionString()};
        for (final UserTable projectWatcher : projectWatchers) {
            notificationTables.add(new NotificationTable(projectWatcher.getId(), action, userId, messageArgs, NotificationType.NEUTRAL));
        }
        this.notificationsDAO.insert(notificationTables);
    }

    public void notifyUsersVersionReviewed(final ProjectVersionTable projectVersionTable, final boolean partial) {
        final List<NotificationTable> notificationTables = new ArrayList<>();
        final ProjectTable projectTable = this.projectsDAO.getById(projectVersionTable.getProjectId());
        this.permissionService.getProjectMemberPermissions(projectVersionTable.getProjectId()).forEach((user, perm) -> {
            if (perm.has(Permission.EditVersion)) {
                notificationTables.add(new NotificationTable(
                    user.getId(),
                    projectTable.getOwnerName() + "/" + projectTable.getSlug() + "/versions/" + projectVersionTable.getVersionString(),
                    this.getHangarUserId(),
                    new String[]{partial ? "notifications.project.reviewedPartial" : "notifications.project.reviewed", projectTable.getSlug(), projectVersionTable.getVersionString()},
                    NotificationType.SUCCESS)
                );
            }
        });
        this.notificationsDAO.insert(notificationTables);
    }
}
