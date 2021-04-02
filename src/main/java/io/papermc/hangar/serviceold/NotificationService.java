package io.papermc.hangar.serviceold;

import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.daoold.NotificationsDao;
import io.papermc.hangar.db.modelold.NotificationsTable;
import io.papermc.hangar.modelold.NotificationType;
import org.postgresql.shaded.com.ongres.scram.common.util.Preconditions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("oldNotificationService")
public class NotificationService extends HangarService {

    private final HangarDao<NotificationsDao> notificationsDao;

    @Autowired
    public NotificationService(HangarDao<NotificationsDao> notificationsDao) {
        this.notificationsDao = notificationsDao;
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

}
