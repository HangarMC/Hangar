package io.papermc.hangar.service.internal.users;

import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.internal.HangarNotificationsDAO;
import io.papermc.hangar.db.dao.internal.table.NotificationsDAO;
import io.papermc.hangar.db.dao.internal.table.projects.ProjectsDAO;
import io.papermc.hangar.model.common.Permission;
import io.papermc.hangar.model.common.roles.OrganizationRole;
import io.papermc.hangar.model.common.roles.ProjectRole;
import io.papermc.hangar.model.db.NotificationTable;
import io.papermc.hangar.model.db.OrganizationTable;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.model.db.projects.ProjectTable;
import io.papermc.hangar.model.db.versions.ProjectVersionTable;
import io.papermc.hangar.model.internal.api.requests.EditMembersForm.Member;
import io.papermc.hangar.model.internal.user.notifications.HangarNotification;
import io.papermc.hangar.model.internal.user.notifications.NotificationType;
import io.papermc.hangar.service.HangarService;
import io.papermc.hangar.service.PermissionService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class NotificationService extends HangarService {

    private final NotificationsDAO notificationsDAO;
    private final HangarNotificationsDAO hangarNotificationsDAO;
    private final ProjectsDAO projectsDAO;
    private final PermissionService permissionService;

    public NotificationService(HangarDao<NotificationsDAO> notificationsDAO, HangarDao<HangarNotificationsDAO> hangarNotificationsDAO, HangarDao<ProjectsDAO> projectsDAO, PermissionService permissionService) {
        this.notificationsDAO = notificationsDAO.get();
        this.hangarNotificationsDAO = hangarNotificationsDAO.get();
        this.projectsDAO = projectsDAO.get();
        this.permissionService = permissionService;
    }

    public List<HangarNotification> getUsersNotifications() {
        return hangarNotificationsDAO.getNotifications(getHangarPrincipal().getId());
    }

    public boolean markNotificationAsRead(long notificationId) {
        return notificationsDAO.markAsRead(notificationId, getHangarPrincipal().getId());
    }

    public void notifyUsersNewVersion(ProjectTable projectTable, ProjectVersionTable projectVersionTable, List<UserTable> projectWatchers) {
        List<NotificationTable> notificationTables = new ArrayList<>();
        for (UserTable projectWatcher : projectWatchers) {
            notificationTables.add(new NotificationTable(
                    projectWatcher.getId(),
                    NotificationType.NEW_PROJECT_VERSION,
                    projectTable.getOwnerName() + "/" + projectTable.getSlug(),
                    projectTable.getId(),
                    new String[]{"notifications.project.newVersion", projectTable.getName(), projectVersionTable.getVersionString()})
            );
        }
        notificationsDAO.insert(notificationTables);
    }

    public void notifyUsersVersionReviewed(ProjectVersionTable projectVersionTable, boolean partial) {
        List<NotificationTable> notificationTables = new ArrayList<>();
        ProjectTable projectTable = projectsDAO.getById(projectVersionTable.getProjectId());
        permissionService.getProjectMemberPermissions(projectVersionTable.getProjectId()).forEach((user, perm) -> {
            if (perm.has(Permission.EditVersion)) {
                if (partial) {
                    notificationTables.add(new NotificationTable(user.getId(), NotificationType.VERSION_REVIEWED_PARTIAL, null, null,
                            new String[]{"notifications.project.reviewedPartial", projectTable.getSlug(), projectVersionTable.getVersionString()}));
                } else {
                    notificationTables.add(new NotificationTable(user.getId(), NotificationType.VERSION_REVIEWED, null, null,
                            new String[]{"notifications.project.reviewed", projectTable.getSlug(), projectVersionTable.getVersionString()}));
                }
            }
        });
        notificationsDAO.insert(notificationTables);
    }

    public void notifyNewProjectMember(Member<ProjectRole> member, long userId, ProjectTable projectTable) {
        notificationsDAO.insert(new NotificationTable(userId, NotificationType.PROJECT_INVITE, null, projectTable.getId(), new String[]{"notifications.project.invite", member.getRole().getTitle(), projectTable.getName()}));
    }

    public void notifyNewOrganizationMember(Member<OrganizationRole> member, long userId, OrganizationTable organizationTable) {
        notificationsDAO.insert(new NotificationTable(userId, NotificationType.ORGANIZATION_INVITE, null, organizationTable.getId(), new String[]{"notifications.organization.invite", member.getRole().getTitle(), organizationTable.getName()}));
    }
}
