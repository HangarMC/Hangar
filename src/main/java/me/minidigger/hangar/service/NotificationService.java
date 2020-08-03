package me.minidigger.hangar.service;

import org.postgresql.shaded.com.ongres.scram.common.util.Preconditions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import me.minidigger.hangar.db.dao.HangarDao;
import me.minidigger.hangar.db.dao.NotificationsDao;
import me.minidigger.hangar.db.dao.UserOrganizationRolesDao;
import me.minidigger.hangar.db.dao.UserProjectRolesDao;
import me.minidigger.hangar.db.model.NotificationsTable;
import me.minidigger.hangar.db.model.OrganizationsTable;
import me.minidigger.hangar.db.model.ProjectsTable;
import me.minidigger.hangar.db.model.RoleTable;
import me.minidigger.hangar.db.model.UserOrganizationRolesTable;
import me.minidigger.hangar.db.model.UserProjectRolesTable;
import me.minidigger.hangar.db.model.Visitable;
import me.minidigger.hangar.model.InviteFilter;
import me.minidigger.hangar.model.NotificationFilter;
import me.minidigger.hangar.model.NotificationType;
import me.minidigger.hangar.model.viewhelpers.InviteSubject;
import me.minidigger.hangar.model.viewhelpers.UserData;
import me.minidigger.hangar.model.viewhelpers.UserRole;

@Service
public class NotificationService {

    private final HangarDao<NotificationsDao> notificationsDao;
    private final HangarDao<UserProjectRolesDao> userProjectRolesDao;
    private final HangarDao<UserOrganizationRolesDao> userOrganizationRolesTable;
    private final UserService userService;

    @Autowired
    public NotificationService(HangarDao<NotificationsDao> notificationsDao, UserService userService, HangarDao<UserProjectRolesDao> userProjectRolesDao, HangarDao<UserOrganizationRolesDao> userOrganizationRolesTable) {
        this.notificationsDao = notificationsDao;
        this.userService = userService;
        this.userProjectRolesDao = userProjectRolesDao;
        this.userOrganizationRolesTable = userOrganizationRolesTable;
    }

    public NotificationsTable sendNotification(long userId, long originId, NotificationType type, String[] messageArgs) {
        Preconditions.checkArgument(messageArgs.length != 0, "messageArgs must be non-empty");
        NotificationsTable notification = new NotificationsTable(userId, type, null, originId, messageArgs);
        notificationsDao.get().insert(notification);
        return notification;
    }

    public boolean markAsRead(long notificationId) {
        return notificationsDao.get().markAsRead(notificationId, userService.getCurrentUser().getId());
    }

    public Map<NotificationsTable, UserData> getNotifications(NotificationFilter filter) {
        return notificationsDao.get().getUserNotifications(userService.getCurrentUser().getId(), filter.getFilter()).entrySet().stream().collect(Collectors.toMap(Entry::getKey, entry -> userService.getUserData(entry.getValue())));
    }

    public Map<UserRole<?>, InviteSubject<?>> getInvites(InviteFilter inviteFilter) {
        long userId = userService.getCurrentUser().getId();
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
