package me.minidigger.hangar.db.dao;

import me.minidigger.hangar.db.model.ProjectChannelsTable;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

public interface ProjectChannelDao {

    @SqlUpdate("insert into project_channels (created_at, name, color, project_id) values (:now, :name, :color, :projectId)")
    @Timestamped
    @GetGeneratedKeys
    ProjectChannelsTable insert(@BindBean ProjectChannelsTable projectChannel);
}
