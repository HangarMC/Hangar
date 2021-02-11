package io.papermc.hangar.db.dao.internal.table.projects;

import io.papermc.hangar.model.common.Color;
import io.papermc.hangar.model.db.projects.ProjectChannelTable;
import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RegisterConstructorMapper(ProjectChannelTable.class)
public interface ProjectChannelsDAO {

    @Timestamped
    @GetGeneratedKeys
    @SqlUpdate("INSERT INTO project_channels (created_at, name, color, project_id, non_reviewed) VALUES (:now, :name, :color, :projectId, :nonReviewed)")
    ProjectChannelTable insert(@BindBean ProjectChannelTable projectChannelTable);

    @SqlQuery("SELECT * FROM project_channels WHERE project_id = :projectId")
    List<ProjectChannelTable> getProjectChannels(long projectId);

    @SqlQuery("SELECT * FROM project_channels WHERE project_id = :projectId AND name = :name AND color = :color")
    ProjectChannelTable getProjectChannel(long projectId, String name, @EnumByOrdinal Color color);

    @SqlQuery("SELECT * FROM project_channels WHERE project_id = :projectId ORDER BY created_at LIMIT 1")
    ProjectChannelTable getFirstChannel(long projectId);
}
