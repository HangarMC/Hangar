package me.minidigger.hangar.service;

import com.ibm.icu.impl.locale.XCldrStub.CollectionUtilities;
import me.minidigger.hangar.db.dao.HangarDao;
import me.minidigger.hangar.db.dao.NotificationsDao;
import me.minidigger.hangar.db.dao.UserOrganizationRolesDao;
import me.minidigger.hangar.db.dao.UserProjectRolesDao;
import me.minidigger.hangar.db.model.NotificationsTable;
import me.minidigger.hangar.db.model.UserOrganizationRolesTable;
import me.minidigger.hangar.db.model.UserProjectRolesTable;
import me.minidigger.hangar.model.InviteFilter;
import me.minidigger.hangar.model.NotificationFilter;
import me.minidigger.hangar.model.NotificationType;
import me.minidigger.hangar.model.viewhelpers.InviteSubject;
import me.minidigger.hangar.model.viewhelpers.UserData;
import me.minidigger.hangar.model.viewhelpers.UserRole;
import org.postgresql.shaded.com.ongres.scram.common.util.Preconditions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    // maybe this should go in RoleService? Also, this looks ugly af
    public Map<UserRole, InviteSubject> getInvites(InviteFilter inviteFilter) {
        long userId = userService.getCurrentUser().getId();
        switch (inviteFilter) {
            case PROJECTS:
                return userProjectRolesDao.get().getUnacceptedRoles(userId).entrySet().stream().collect(Collectors.toMap(entry -> new UserRole<>(entry.getKey()), entry -> InviteSubject.PROJECT.with(entry.getValue())));
            case ORGANIZATIONS:
                return userOrganizationRolesTable.get().getUnacceptedRoles(userId).entrySet().stream().collect(Collectors.toMap(entry -> new UserRole<>(entry.getKey()), entry -> InviteSubject.ORGANIZATION.with(entry.getValue())));
            case ALL:
                return Stream.concat(
                        userProjectRolesDao.get().getUnacceptedRoles(userId).entrySet().stream().collect(Collectors.toMap(entry -> new UserRole<>(entry.getKey()), entry -> InviteSubject.PROJECT.with(entry.getValue()))).entrySet().stream(),
                        userOrganizationRolesTable.get().getUnacceptedRoles(userId).entrySet().stream().collect(Collectors.toMap(entry -> new UserRole<>(entry.getKey()), entry -> InviteSubject.ORGANIZATION.with(entry.getValue()))).entrySet().stream()
                ).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
            default:
                return new LinkedHashMap<>(); // make IJ shut it
        }
    }
}
