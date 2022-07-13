package io.papermc.hangar.service.internal.users;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.db.dao.internal.HangarNotificationsDAO;
import io.papermc.hangar.db.dao.internal.projects.HangarProjectsDAO;
import io.papermc.hangar.db.dao.internal.table.NotificationsDAO;
import io.papermc.hangar.db.dao.internal.table.projects.ProjectsDAO;
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

@Service
public class NotificationService extends HangarComponent {

    private final NotificationsDAO notificationsDAO;
    private final HangarNotificationsDAO hangarNotificationsDAO;
    private final HangarProjectsDAO hangarProjectsDAO;
    private final ProjectsDAO projectsDAO;
    private final PermissionService permissionService;

    public NotificationService(NotificationsDAO notificationsDAO, HangarNotificationsDAO hangarNotificationsDAO, final HangarProjectsDAO hangarProjectsDAO, ProjectsDAO projectsDAO, PermissionService permissionService) {
        this.notificationsDAO = notificationsDAO;
        this.hangarNotificationsDAO = hangarNotificationsDAO;
        this.hangarProjectsDAO = hangarProjectsDAO;
        this.projectsDAO = projectsDAO;
        this.permissionService = permissionService;
    }

    public List<HangarNotification> getUsersNotifications(int amount) {
        return hangarNotificationsDAO.getNotifications(getHangarPrincipal().getId(), amount);
    }

    public long getUnreadNotifications() {
        return notificationsDAO.getUnreadNotificationCount(getHangarPrincipal().getId());
    }

    public boolean markNotificationAsRead(long notificationId) {
        return notificationsDAO.markAsRead(notificationId, getHangarPrincipal().getId());
    }

    public void notifyVisibilityChange(final ProjectTable projectTable, final Visibility visibility, @Nullable final String comment) {
        final NotificationType notificationType = visibility == Visibility.SOFTDELETE || visibility == Visibility.NEEDSCHANGES
            || visibility == Visibility.NEEDSAPPROVAL ? NotificationType.WARNING : NotificationType.SUCCESS;
        final String[] message = comment != null && !comment.isBlank()
            ? new String[]{"notifications.project.visibilityChangedComment", projectTable.getName(), visibility.name(), comment}
            : new String[]{"notifications.project.visibilityChanged", projectTable.getName(), visibility.name()};
        notifyProjectMembers(projectTable.getProjectId(), getHangarUserId(), projectTable.getOwnerName(), projectTable.getSlug(), notificationType, message);
    }

    public NotificationTable notify(final long userId, @Nullable final String action, @Nullable final Long originId, final NotificationType notificationType, final String[] message) {
        return notificationsDAO.insert(new NotificationTable(userId, action, originId, message, notificationType));
    }

    public List<NotificationTable> notifyProjectMembers(final long projectId, @Nullable final Long originId,
                                                        final String owner, final String slug, final NotificationType notificationType, final String[] message) {
        final List<NotificationTable> notifications = new ArrayList<>();
        final List<JoinableMember<ProjectRoleTable>> members = hangarProjectsDAO.getProjectMembers(projectId, null, false);
        for (final JoinableMember<ProjectRoleTable> member : members) {
            notifications.add(new NotificationTable(
                member.getUser().getUserId(),
                owner + "/" + slug,
                originId,
                message, notificationType)
            );
        }
        return notificationsDAO.insert(notifications);
    }

    public void notifyUsersNewVersion(ProjectTable projectTable, ProjectVersionTable projectVersionTable, List<UserTable> projectWatchers) {
        List<NotificationTable> notificationTables = new ArrayList<>();
        for (UserTable projectWatcher : projectWatchers) {
            notificationTables.add(new NotificationTable(
                projectWatcher.getId(),
                projectTable.getOwnerName() + "/" + projectTable.getSlug() + "/versions/" + projectVersionTable.getVersionString(),
                projectTable.getId(),
                new String[]{"notifications.project.newVersion", projectTable.getName(), projectVersionTable.getVersionString()}, NotificationType.NEUTRAL)
            );
        }
        notificationsDAO.insert(notificationTables);
    }

    public void notifyUsersVersionReviewed(ProjectVersionTable projectVersionTable, boolean partial) {
        List<NotificationTable> notificationTables = new ArrayList<>();
        ProjectTable projectTable = projectsDAO.getById(projectVersionTable.getProjectId());
        permissionService.getProjectMemberPermissions(projectVersionTable.getProjectId()).forEach((user, perm) -> {
            if (perm.has(Permission.EditVersion)) {
                notificationTables.add(new NotificationTable(
                    user.getId(),
                    projectTable.getOwnerName() + "/" + projectTable.getSlug() + "/versions/" + projectVersionTable.getVersionString(),
                    projectTable.getId(),
                    new String[]{partial ? "notifications.project.reviewedPartial" : "notifications.project.reviewed", projectTable.getSlug(), projectVersionTable.getVersionString()},
                    NotificationType.SUCCESS)
                );
            }
        });
        notificationsDAO.insert(notificationTables);
    }
}
