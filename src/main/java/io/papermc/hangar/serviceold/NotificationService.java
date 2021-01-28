package io.papermc.hangar.serviceold;

import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.daoold.NotificationsDao;
import io.papermc.hangar.db.daoold.UserOrganizationRolesDao;
import io.papermc.hangar.db.daoold.UserProjectRolesDao;
import io.papermc.hangar.db.modelold.NotificationsTable;
import io.papermc.hangar.db.modelold.OrganizationsTable;
import io.papermc.hangar.db.modelold.ProjectsTable;
import io.papermc.hangar.db.modelold.UserOrganizationRolesTable;
import io.papermc.hangar.db.modelold.UserProjectRolesTable;
import io.papermc.hangar.db.modelold.UsersTable;
import io.papermc.hangar.model.NotificationType;
import io.papermc.hangar.modelold.InviteFilter;
import io.papermc.hangar.modelold.NotificationFilter;
import io.papermc.hangar.modelold.viewhelpers.InviteSubject;
import io.papermc.hangar.modelold.viewhelpers.UserRole;
import org.postgresql.shaded.com.ongres.scram.common.util.Preconditions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

@Service
public class NotificationService extends HangarService {

    private final HangarDao<NotificationsDao> notificationsDao;
    private final HangarDao<UserProjectRolesDao> userProjectRolesDao;
    private final HangarDao<UserOrganizationRolesDao> userOrganizationRolesTable;

    @Autowired
    public NotificationService(HangarDao<NotificationsDao> notificationsDao, HangarDao<UserProjectRolesDao> userProjectRolesDao, HangarDao<UserOrganizationRolesDao> userOrganizationRolesTable) {
        this.notificationsDao = notificationsDao;
        this.userProjectRolesDao = userProjectRolesDao;
        this.userOrganizationRolesTable = userOrganizationRolesTable;
    }

    public NotificationsTable sendNotification(long userId, Long originId, NotificationType type, String[] messageArgs) {
        return sendNotification(userId, originId, type, messageArgs, null);
    }

    public NotificationsTable sendNotification(long userId, Long originId, NotificationType type, String[] messageArgs, String action) {
        Preconditions.checkArgument(messageArgs.length != 0, "messageArgs must be non-empty");
        NotificationsTable notification = new NotificationsTable(userId, type, action, originId, messageArgs);
        notificationsDao.get().insert(notification);
        return notification;
    }

    public boolean markAsRead(long notificationId) {
        return notificationsDao.get().markAsRead(notificationId, getCurrentUser().getId());
    }

    public Map<NotificationsTable, UsersTable> getNotifications(NotificationFilter filter) {
        return notificationsDao.get().getUserNotifications(getCurrentUser().getId(), filter.getFilter())
                .entrySet().stream().collect(HashMap::new, (m, v) -> {
                    if (v.getValue() == null || v.getValue().getName() == null) {
                        m.put(v.getKey(), null);
                    } else {
                        m.put(v.getKey(), v.getValue());
                    }
                }, HashMap::putAll);
    }

    public Map<UserRole<?>, InviteSubject<?>> getInvites(InviteFilter inviteFilter) {
        long userId = getCurrentUser().getId();
        Map<UserRole<?>, InviteSubject<?>> result = new HashMap<>();
        switch (inviteFilter) {
            case PROJECTS:
                for (Entry<UserProjectRolesTable, ProjectsTable> entry : userProjectRolesDao.get().getUnacceptedRoles(userId).entrySet()) {
                    result.put(new UserRole<>(entry.getKey()), InviteSubject.PROJECT.with(entry.getValue()));
                }
                return result;
            case ORGANIZATIONS:
                for (Entry<UserOrganizationRolesTable, OrganizationsTable> entry : userOrganizationRolesTable.get().getUnacceptedRoles(userId).entrySet()) {
                    result.put(new UserRole<>(entry.getKey()), InviteSubject.ORGANIZATION.with(entry.getValue()));
                }
                return result;
            case ALL:
                for (Entry<UserProjectRolesTable, ProjectsTable> entry : userProjectRolesDao.get().getUnacceptedRoles(userId).entrySet()) {
                    result.put(new UserRole<>(entry.getKey()), InviteSubject.PROJECT.with(entry.getValue()));
                }
                for (Entry<UserOrganizationRolesTable, OrganizationsTable> entry : userOrganizationRolesTable.get().getUnacceptedRoles(userId).entrySet()) {
                    result.put(new UserRole<>(entry.getKey()), InviteSubject.ORGANIZATION.with(entry.getValue()));
                }
                return result;
            default:
                return result;
        }
    }
}
