package io.papermc.hangar.db.dao.internal.table.versions;

import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.model.db.versions.ProjectVersionTable;
import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.jdbi.v3.core.enums.EnumStrategy;
import org.jdbi.v3.sqlobject.SingleValue;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.config.UseEnumStrategy;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RegisterConstructorMapper(ProjectVersionTable.class)
public interface ProjectVersionsDAO {

    @Timestamped
    @GetGeneratedKeys
    @SqlUpdate("INSERT INTO project_versions " +
            "(created_at, version_string, description, project_id, channel_id, file_size, hash, file_name, external_url, author_id, visibility, create_forum_post) VALUES " +
            "(:now, :versionString, :description, :projectId, :channelId, :fileSize, :hash, :fileName, :externalUrl, :authorId, :visibility, :createForumPost)")
    ProjectVersionTable insert(@BindBean ProjectVersionTable projectVersionTable);

    @GetGeneratedKeys
    @SqlUpdate("UPDATE project_versions SET visibility = :visibility, reviewer_id = :reviewerId, approved_at = :approvedAt, description = :description, " +
            "review_state = :reviewState, external_url = :externalUrl, post_id = :postId " +
            "WHERE id = :id")
    ProjectVersionTable update(@BindBean ProjectVersionTable projectVersionsTable);

    @SqlUpdate("DELETE FROM project_versions WHERE id = :id")
    void delete(@BindBean ProjectVersionTable projectVersionTable);

    @SqlQuery("SELECT * FROM project_versions pv WHERE pv.id = :versionId")
    ProjectVersionTable getProjectVersionTable(long versionId);

    @SqlQuery("SELECT * FROM project_versions WHERE project_id = :projectId")
    List<ProjectVersionTable> getProjectVersions(long projectId);

    @SqlQuery("SELECT pv.*" +
            "   FROM project_versions pv" +
            "       JOIN project_channels pc ON pv.channel_id = pc.id" +
            "   WHERE pc.id = :channelId" +
            "   ORDER BY pv.created_at DESC")
    ProjectVersionTable getLastVersionOnChannel(long channelId);

    @SqlQuery("SELECT pv.* FROM project_versions pv" +
            "   JOIN projects p ON pv.project_id = p.id" +
            "   JOIN project_version_platform_dependencies pvpd ON pv.id = pvpd.version_id" +
            "   JOIN platform_versions v ON pvpd.platform_version_id = v.id" +
            "   WHERE" +
            "       lower(p.owner_name) = lower(:author) AND" +
            "       lower(p.slug) = lower(:slug) AND" +
            "       pv.version_string = :versionString AND" +
            "       v.platform = :platform" +
            "   LIMIT 1")
    ProjectVersionTable getProjectVersionTable(String author, String slug, String versionString, @EnumByOrdinal Platform platform);

    @SqlQuery("SELECT * FROM project_versions WHERE project_id = :projectId AND hash = :hash AND version_string = :versionString")
    ProjectVersionTable getProjectVersionTableFromHashAndName(long projectId, String hash, String versionString);

    @SingleValue
    @UseEnumStrategy(EnumStrategy.BY_ORDINAL)
    @SqlQuery("SELECT array_agg(DISTINCT plv.platform)" +
            "   FROM project_versions pv" +
            "       JOIN project_version_platform_dependencies pvpd ON pv.id = pvpd.version_id" +
            "       JOIN platform_versions plv ON pvpd.platform_version_id = plv.id" +
            "   WHERE pv.id = :versionId GROUP BY pv.id")
    List<Platform> getVersionPlatforms(long versionId);
}
