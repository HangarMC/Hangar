package io.papermc.hangar.db.dao.internal.table.projects;

import io.papermc.hangar.model.common.Color;
import io.papermc.hangar.model.db.projects.ProjectChannelTable;
import java.util.List;
import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.jdbi.v3.spring.JdbiRepository;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

@JdbiRepository
@RegisterConstructorMapper(ProjectChannelTable.class)
public interface ProjectChannelsDAO {

    @Timestamped
    @GetGeneratedKeys
    @SqlUpdate("INSERT INTO project_channels (created_at, \"name\", description, color, project_id, flags) VALUES (:now, :name, :description, :color, :projectId, :flags)")
    ProjectChannelTable insert(@BindBean ProjectChannelTable projectChannelTable);

    @SqlUpdate("UPDATE project_channels SET name = :name, description = :description, color = :color, flags = :flags WHERE id = :id")
    void update(@BindBean ProjectChannelTable projectChannelTable);

    @SqlUpdate("DELETE FROM project_channels WHERE id = :id")
    void delete(@BindBean ProjectChannelTable projectChannelTable);

    @SqlQuery("SELECT * FROM project_channels WHERE project_id = :projectId")
    List<ProjectChannelTable> getProjectChannels(long projectId);

    @SqlQuery("SELECT * FROM project_channels WHERE project_id = :projectId AND lower(name) = lower(:name)")
    ProjectChannelTable getProjectChannel(long projectId, String name);

    @SqlQuery("SELECT * FROM project_channels WHERE id = :channelId")
    ProjectChannelTable getProjectChannel(long channelId);
}
