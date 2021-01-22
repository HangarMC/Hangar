package io.papermc.hangar.db.dao;

import io.papermc.hangar.model.db.NotificationTable;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.springframework.stereotype.Repository;

@Repository
@RegisterConstructorMapper(NotificationTable.class)
public interface NotificationsDAO {

    @SqlQuery("SELECT exists(SELECT 1 FROM notifications n WHERE n.user_id = :userId AND n.read IS FALSE)")
    boolean hasUnreadNotifications(long userId);

    @SqlQuery("SELECT exists(SELECT 1 FROM project_flags WHERE is_resolved IS FALSE)")
    boolean hasUnresolvedFlags();

    @SqlQuery("SELECT exists(SELECT 1 FROM projects p WHERE p.owner_id = :userId AND p.visibility = 3)")
    boolean hasProjectApprovals(long userId);

    @SqlQuery("SELECT exists(SELECT 1 FROM project_versions pv WHERE pv.review_state = 0)")
    boolean hasReviewQueue();
}
