package io.papermc.hangar.db.dao.internal.table;

import io.papermc.hangar.model.db.NotificationTable;
import java.util.Collection;
import java.util.List;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlBatch;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.springframework.stereotype.Repository;

@Repository
@RegisterConstructorMapper(NotificationTable.class)
public interface NotificationsDAO {

    @Timestamped
    @GetGeneratedKeys
    @SqlUpdate("INSERT INTO notifications (created_at, user_id, type, action, origin_id, message_args) VALUES (:now, :userId, :type, :action, :originId, :messageArgs)")
    NotificationTable insert(@BindBean NotificationTable notificationTable);

    @Timestamped
    @GetGeneratedKeys
    @SqlBatch("INSERT INTO notifications (created_at, user_id, type, action, origin_id, message_args) VALUES (:now, :userId, :type, :action, :originId, :messageArgs)")
    List<NotificationTable> insert(@BindBean Collection<NotificationTable> notificationTables);

    @SqlUpdate("UPDATE notifications SET read = TRUE WHERE id = :notificationId AND user_id = :userId")
    boolean markAsRead(long notificationId, long userId);

    @SqlUpdate("UPDATE notifications SET read = TRUE WHERE read = FALSE AND user_id = :userId")
    void markAllAsRead(long userId);

    @SqlQuery("SELECT count(*)" +
        "   FROM notifications n" +
        "   WHERE n.user_id = :userId" +
        "       AND n.read IS FALSE")
    long getUnreadNotificationCount(long userId);

    @SqlQuery("SELECT (SELECT count(*) FROM user_project_roles upr WHERE upr.user_id = :userId AND upr.accepted IS FALSE) + " +
        "       (SELECT count(*) FROM user_organization_roles uor WHERE uor.user_id = :userId AND uor.accepted IS FALSE) AS count")
    long getUnansweredInvites(long userId);
}
