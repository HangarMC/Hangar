package io.papermc.hangar.db.dao;

import io.papermc.hangar.db.modelold.ProjectChannelsTable;
import io.papermc.hangar.modelold.Color;
import io.papermc.hangar.service.project.ChannelService;
import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.config.ValueColumn;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Define;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.stringtemplate4.UseStringTemplateEngine;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
@RegisterBeanMapper(ProjectChannelsTable.class)
public interface ProjectChannelDao {

    @SqlUpdate("insert into project_channels (created_at, name, color, project_id, is_non_reviewed) values (:now, :name, :color, :projectId, :isNonReviewed)")
    @Timestamped
    @GetGeneratedKeys
    ProjectChannelsTable insert(@BindBean ProjectChannelsTable projectChannel);

    @SqlUpdate("UPDATE project_channels SET name = :name, color = :color, is_non_reviewed = :nonReviewed WHERE project_id = :projectId AND name = :oldName")
    void update(long projectId, String oldName, String name, @EnumByOrdinal Color color, boolean nonReviewed);

    @SqlQuery("SELECT * FROM project_channels WHERE project_id = :projectId ORDER BY created_at LIMIT 1")
    ProjectChannelsTable getFirstChannel(long projectId);

    @SqlQuery("SELECT * FROM project_channels WHERE project_id = :projectId")
    List<ProjectChannelsTable> getProjectChannels(long projectId);

    @UseStringTemplateEngine
    @SqlQuery("SELECT " +
            "CASE WHEN countq.count > <maxChannels> THEN 'MAX_CHANNELS' " +
            "     WHEN '<channelName>' IN (SELECT \"name\" FROM project_channels WHERE project_id = :projectId) THEN 'DUPLICATE_NAME' " +
            "     WHEN <colorValue> IN (SELECT color FROM project_channels WHERE project_id = :projectId) THEN 'UNIQUE_COLOR' " +
            "END " +
            "FROM (SELECT COUNT(*) count FROM project_channels WHERE project_id = :projectId) AS countq")
    ChannelService.InvalidChannelCreationReason validateChannelCreation(long projectId, @Define String channelName, @Define long colorValue, @Define int maxChannels);

    @UseStringTemplateEngine
    @SqlQuery("SELECT " +
            "CASE WHEN '<channelName>' IN (SELECT \"name\" FROM project_channels WHERE project_id = :projectId AND name != :oldChannelName) THEN 'DUPLICATE_NAME' " +
            "     WHEN <colorValue> IN (SELECT color FROM project_channels WHERE project_id = :projectId AND name != :oldChannelName) THEN 'UNIQUE_COLOR' " +
            "END")
    ChannelService.InvalidChannelCreationReason validateChannelUpdate(long projectId, String oldChannelName, @Define String channelName, @Define long colorValue);

    @SqlQuery("SELECT * FROM project_channels WHERE project_id = :projectId AND (name = :channelName OR id = :channelId)")
    ProjectChannelsTable getProjectChannel(long projectId, String channelName, Long channelId);

    @SqlQuery("SELECT pc.* FROM project_channels pc" +
              "     JOIN project_versions pv ON pc.id = pv.channel_id" +
              " WHERE pv.id = :versionId AND pv.project_id = :projectId")
    ProjectChannelsTable getVersionsChannel(long projectId, long versionId);

    @ValueColumn("version_count")
    @SqlQuery("SELECT pc.*, COUNT(pv.id) as version_count FROM project_channels pc" +
              "     LEFT JOIN project_versions pv ON pc.id = pv.channel_id" +
              " WHERE pc.project_id = :projectId" +
              " GROUP BY pc.id")
    Map<ProjectChannelsTable, Integer> getChannelsWithVersionCount(long projectId);
}
