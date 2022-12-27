package io.papermc.hangar.db.dao.internal.projects;

import io.papermc.hangar.model.internal.projects.HangarProjectFlagNotification;
import java.util.List;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.springframework.stereotype.Repository;

@Repository
public interface HangarProjectFlagNofiticationsDAO {

    @RegisterConstructorMapper(HangarProjectFlagNotification.class)
    @SqlQuery("SELECT fn.id, n.user_id, n.type, n.message_args message, u.name AS origin_user_name" +
        "   FROM project_flag_notifications fn" +
        "   LEFT OUTER JOIN notifications n ON fn.notification_id = n.id" +
        "   LEFT OUTER JOIN users u ON u.id = n.origin_id" +
        "   WHERE fn.flag_id = :flagId")
    List<HangarProjectFlagNotification> getNotifications(long flagId);
}
