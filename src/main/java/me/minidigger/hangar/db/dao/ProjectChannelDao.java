package me.minidigger.hangar.db.dao;

import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.springframework.stereotype.Repository;

import me.minidigger.hangar.db.model.ProjectChannelsTable;

import java.util.List;

@Repository
@RegisterBeanMapper(ProjectChannelsTable.class)
public interface ProjectChannelDao {

    @SqlUpdate("insert into project_channels (created_at, name, color, project_id) values (:now, :name, :color, :projectId)")
    @Timestamped
    @GetGeneratedKeys
    ProjectChannelsTable insert(@BindBean ProjectChannelsTable projectChannel);

    @SqlQuery("SELECT * FROM project_channels WHERE project_id = :projectId")
    List<ProjectChannelsTable> getProjectChannels(long projectId);
}
