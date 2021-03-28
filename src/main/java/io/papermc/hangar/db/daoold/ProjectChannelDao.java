package io.papermc.hangar.db.daoold;

import io.papermc.hangar.db.modelold.ProjectChannelsTable;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.springframework.stereotype.Repository;

@Repository
@Deprecated(forRemoval = true)
@RegisterBeanMapper(ProjectChannelsTable.class)
public interface ProjectChannelDao {

    @SqlQuery("SELECT * FROM project_channels WHERE project_id = :projectId AND (name = :channelName OR id = :channelId)")
    ProjectChannelsTable getProjectChannel(long projectId, String channelName, Long channelId);

    @SqlQuery("SELECT pc.* FROM project_channels pc" +
              "     JOIN project_versions pv ON pc.id = pv.channel_id" +
              " WHERE pv.id = :versionId AND pv.project_id = :projectId")
    ProjectChannelsTable getVersionsChannel(long projectId, long versionId);

}
