package io.papermc.hangar.service.internal.users;

import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.internal.HangarNotificationsDAO;
import io.papermc.hangar.db.dao.internal.table.NotificationsDAO;
import io.papermc.hangar.model.db.NotificationTable;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.model.db.projects.ProjectTable;
import io.papermc.hangar.model.db.versions.ProjectVersionTable;
import io.papermc.hangar.model.internal.user.notifications.HangarNotification;
import io.papermc.hangar.model.internal.user.notifications.NotificationType;
import io.papermc.hangar.service.HangarService;
import io.papermc.hangar.service.internal.projects.ProjectService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class NotificationService extends HangarService {

    private final NotificationsDAO notificationsDAO;
    private final HangarNotificationsDAO hangarNotificationsDAO;
    private final ProjectService projectService;

    public NotificationService(HangarDao<NotificationsDAO> notificationsDAO, HangarDao<HangarNotificationsDAO> hangarNotificationsDAO, ProjectService projectService) {
        this.notificationsDAO = notificationsDAO.get();
        this.hangarNotificationsDAO = hangarNotificationsDAO.get();
        this.projectService = projectService;
    }

    public List<HangarNotification> getUsersNotifications() {
        return hangarNotificationsDAO.getNotifications(getHangarPrincipal().getId());
    }

    public boolean markNotificationAsRead(long notificationId) {
        return notificationsDAO.markAsRead(notificationId, getHangarPrincipal().getId());
    }

    public void notifyUsersNewVersion(ProjectTable projectTable, ProjectVersionTable projectVersionTable) {
        List<NotificationTable> notificationTables = new ArrayList<>();
        for (UserTable projectWatcher : projectService.getProjectWatchers(projectTable.getId())) {
            notificationTables.add(new NotificationTable(
                    projectWatcher.getId(),
                    NotificationType.NEW_PROJECT_VERSION,
                    projectTable.getOwnerName() + "/" + projectTable.getSlug(),
                    projectTable.getId(),
                    new String[]{"notification.project.newVersion", projectTable.getName(), projectVersionTable.getVersionString()})
            );
        }
        notificationsDAO.insert(notificationTables);
    }
}
