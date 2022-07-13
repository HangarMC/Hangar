package io.papermc.hangar.db.dao.internal.table.projects;

import io.papermc.hangar.model.db.projects.ProjectFlagNotificationTable;
import io.papermc.hangar.model.db.projects.ProjectFlagTable;
import java.util.Collection;
import java.util.List;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlBatch;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.springframework.stereotype.Repository;

@Repository
@RegisterConstructorMapper(ProjectFlagNotificationTable.class)
public interface ProjectFlagNotificationsDAO {

    @GetGeneratedKeys
    @SqlUpdate("INSERT INTO project_flag_notifications (flag_id, notification_id) VALUES (:flagId, :notificationId)")
    ProjectFlagNotificationTable insert(@BindBean ProjectFlagNotificationTable projectFlagNotificationTable);

    @SqlBatch("INSERT INTO project_flag_notifications (flag_id, notification_id) VALUES (:flagId, :notificationId)")
    void insert(@BindBean Collection<ProjectFlagNotificationTable> projectFlagNotificationTables);

    @SqlQuery("SELECT * FROM project_flag_notifications WHERE flag_id = :flagId")
    List<ProjectFlagTable> notificationsForFlag(long flagId);
}
