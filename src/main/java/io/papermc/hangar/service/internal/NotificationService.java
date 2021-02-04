package io.papermc.hangar.service.internal;

import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.internal.HangarNotificationsDAO;
import io.papermc.hangar.db.dao.internal.table.NotificationsDAO;
import io.papermc.hangar.model.internal.user.notifications.HangarNotification;
import io.papermc.hangar.service.HangarService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService extends HangarService {

    private final NotificationsDAO notificationsDAO;
    private final HangarNotificationsDAO hangarNotificationsDAO;

    public NotificationService(HangarDao<NotificationsDAO> notificationsDAO, HangarDao<HangarNotificationsDAO> hangarNotificationsDAO) {
        this.notificationsDAO = notificationsDAO.get();
        this.hangarNotificationsDAO = hangarNotificationsDAO.get();
    }

    public List<HangarNotification> getUsersNotifications() {
        return hangarNotificationsDAO.getNotifications(getHangarPrincipal().getId());
    }

    public boolean markNotificationAsRead(long notificationId) {
        return notificationsDAO.markAsRead(notificationId, getHangarPrincipal().getId());
    }
}
