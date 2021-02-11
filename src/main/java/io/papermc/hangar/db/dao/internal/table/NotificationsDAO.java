package io.papermc.hangar.db.dao.internal.table;

import io.papermc.hangar.model.db.NotificationTable;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlBatch;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
@RegisterConstructorMapper(NotificationTable.class)
public interface NotificationsDAO {

    @Timestamped
    @GetGeneratedKeys
    @SqlUpdate("INSERT INTO notifications (created_at, user_id, notification_type, action, origin_id, message_args) VALUES (:now, :userId, :notificationType, :action, :originId, :messageArgs)")
    NotificationTable insert(@BindBean NotificationTable notificationTable);

    @Timestamped
    @SqlBatch("INSERT INTO notifications (created_at, user_id, notification_type, action, origin_id, message_args) VALUES (:now, :userId, :notificationType, :action, :originId, :messageArgs)")
    void insert(@BindBean Collection<NotificationTable> notificationTables);

    @SqlUpdate("UPDATE notifications SET read = TRUE WHERE id = :notificationId AND user_id = :userId")
    boolean markAsRead(long notificationId, long userId);

    @SqlQuery("SELECT count(*)" +
            "   FROM notifications n" +
            "   WHERE n.user_id = :userId" +
            "       AND n.read IS FALSE")
    long getUnreadNotificationCount(long userId);

    @SqlQuery("SELECT count(*)" +
            "   FROM project_flags pf" +
            "   WHERE pf.resolved IS FALSE")
    long getUnresolvedFlagsCount();

    @SqlQuery("SELECT count(*)" +
            "   FROM projects p " +
            "   WHERE p.visibility = 3")
    long getProjectApprovalsCount();

    @SqlQuery("SELECT count(*)" +
            "   FROM project_versions pv" +
            "   WHERE pv.review_state = 0")
    long getReviewQueueCount();
}
