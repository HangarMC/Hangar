package io.papermc.hangar.db.dao.internal.table.projects;

import io.papermc.hangar.model.db.projects.ProjectChannelTable;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.springframework.stereotype.Repository;

@Repository
@RegisterConstructorMapper(ProjectChannelTable.class)
public interface ProjectChannelsDAO {

    @Timestamped
    @GetGeneratedKeys
    @SqlUpdate("INSERT INTO project_channels (created_at, name, color, project_id, non_reviewed) VALUES (:now, :name, :color, :projectId, :nonReviewed)")
    ProjectChannelTable insert(@BindBean ProjectChannelTable projectChannelTable);
}
