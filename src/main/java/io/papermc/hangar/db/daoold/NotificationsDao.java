package io.papermc.hangar.db.daoold;

import io.papermc.hangar.db.modelold.NotificationsTable;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.springframework.stereotype.Repository;

@Repository
@RegisterBeanMapper(NotificationsTable.class)
public interface NotificationsDao {

    @Timestamped
    @GetGeneratedKeys
    @SqlUpdate("INSERT INTO notifications (created_at, user_id, notification_type, action, origin_id, message_args) VALUES (:now, :userId, :notificationType, :action, :originId, :messageArgs)")
    NotificationsTable insert(@BindBean NotificationsTable notificationsTable);

    // For HeaderData
    @SqlQuery("SELECT exists(SELECT 1 FROM notifications n WHERE n.user_id = :userId AND n.read IS FALSE)")
    boolean hasUnreadNotifications(long userId);

    @SqlQuery("SELECT exists(SELECT 1 FROM project_flags WHERE is_resolved IS FALSE)")
    boolean hasUnresolvedFlags();

    @SqlQuery("SELECT exists(SELECT 1 FROM projects p WHERE p.owner_id = :userId AND p.visibility = 3)")
    boolean hasProjectApprovals(long userId);

    @SqlQuery("SELECT exists(SELECT 1 FROM project_versions pv WHERE pv.review_state = 0)")
    boolean hasReviewQueue();
}
