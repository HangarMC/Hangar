package io.papermc.hangar.db.dao;

import io.papermc.hangar.db.customtypes.JSONB;
import io.papermc.hangar.db.mappers.PlatformDependencyMapper;
import io.papermc.hangar.db.mappers.VersionDependenciesMapper;
import io.papermc.hangar.db.model.ProjectVersionTagsTable;
import io.papermc.hangar.db.model.ProjectVersionsTable;
import io.papermc.hangar.model.generated.ReviewState;
import io.papermc.hangar.model.viewhelpers.ReviewQueueEntry;
import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.config.RegisterColumnMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlBatch;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RegisterColumnMapper(VersionDependenciesMapper.class)
@RegisterColumnMapper(PlatformDependencyMapper.class)
@RegisterBeanMapper(ProjectVersionsTable.class)
@RegisterBeanMapper(ProjectVersionTagsTable.class)
public interface ProjectVersionDao {

    @Timestamped
    @GetGeneratedKeys
    @SqlUpdate("INSERT INTO project_versions " +
            "(created_at, version_string, dependencies, platforms, description, project_id, channel_id, file_size, hash, file_name, author_id, create_forum_post, external_url) VALUES " +
            "(:now, :versionString, :dependenciesJson, :platformsJson, :description, :projectId, :channelId, :fileSize, :hash, :fileName, :authorId, :createForumPost, :externalUrl)")
    ProjectVersionsTable insert(@BindBean ProjectVersionsTable projectVersionsTable, JSONB dependenciesJson, JSONB platformsJson);

    @SqlUpdate("UPDATE project_versions SET visibility = :visibility, reviewer_id = :reviewerId, approved_at = :approvedAt, description = :description, " +
               "review_state = :reviewState, external_url = :externalUrl " +
               "WHERE id = :id")
    void update(@BindBean ProjectVersionsTable projectVersionsTable);

    @SqlUpdate("DELETE FROM project_versions WHERE id = :id")
    void deleteVersion(long id);

    @RegisterBeanMapper(ReviewQueueEntry.class)
    @SqlQuery("SELECT sq.project_author," +
            "       sq.project_slug," +
            "       sq.project_name," +
            "       sq.version_string," +
            "       sq.version_string_url," +
            "       sq.version_created_at," +
            "       sq.channel_name," +
            "       sq.channel_color," +
            "       sq.version_author," +
            "       sq.reviewer_id," +
            "       sq.reviewer_name," +
            "       sq.review_started," +
            "       sq.review_ended" +
            "  FROM (SELECT pu.name                                                                  AS project_author," +
            "               p.name                                                                   AS project_name," +
            "               p.slug                                                                   AS project_slug," +
            "               v.version_string," +
            "               v.version_string || '.' || v.id                                          AS version_string_url," +
            "               v.created_at                                                             AS version_created_at," +
            "               c.name                                                                   AS channel_name," +
            "               c.color                                                                  AS channel_color," +
            "               vu.name                                                                  AS version_author," +
            "               r.user_id                                                                AS reviewer_id," +
            "               ru.name                                                                  AS reviewer_name," +
            "               r.created_at                                                             AS review_started," +
            "               r.ended_at                                                               AS review_ended," +
            "               row_number() OVER (PARTITION BY (p.id, v.id) ORDER BY r.created_at DESC) AS row" +
            "          FROM project_versions v" +
            "                 LEFT JOIN users vu ON v.author_id = vu.id" +
            "                 INNER JOIN project_channels c ON v.channel_id = c.id" +
            "                 INNER JOIN projects p ON v.project_id = p.id" +
            "                 INNER JOIN users pu ON p.owner_id = pu.id" +
            "                 LEFT JOIN project_version_reviews r ON v.id = r.version_id" +
            "                 LEFT JOIN users ru ON ru.id = r.user_id" +
            "          WHERE v.review_state = :reviewState" +
            "            AND p.visibility != 5" +
            "            AND v.visibility != 5) sq" +
            "  WHERE row = 1" +
            "  ORDER BY sq.project_name DESC, sq.version_string DESC")
    List<ReviewQueueEntry> getQueue(@EnumByOrdinal ReviewState reviewState);

    @SqlQuery("SELECT * FROM project_versions WHERE project_id = :projectId ORDER BY created_at LIMIT 1")
    ProjectVersionsTable getMostRecentVersion(long projectId);

    @SqlQuery("SELECT * FROM project_versions WHERE project_id = :projectId AND (hash = :hash OR lower(version_string) = lower(:versionString))")
    ProjectVersionsTable getProjectVersion(long projectId, String hash, String versionString);

    @SqlQuery("SELECT * FROM project_versions WHERE project_id = :projectId AND (hash = :hash OR id = :versionId)")
    ProjectVersionsTable getProjectVersion(long projectId, String hash, long versionId);

    @SqlQuery("SELECT * FROM project_versions WHERE project_id = :projectId ORDER BY created_at DESC")
    List<ProjectVersionsTable> getProjectVersions(long projectId);

    @GetGeneratedKeys
    @SqlBatch("INSERT INTO project_version_tags (version_id, name, data, color) VALUES (:versionId, :name, :data, :color)")
    List<ProjectVersionTagsTable> insertTags(@BindBean List<ProjectVersionTagsTable> tags);

    @SqlUpdate("INSERT INTO project_version_tags (version_id, name, data, color) VALUES (:versionId, :name, :data, :color)")
    void insertTag(@BindBean ProjectVersionTagsTable projectVersionTagsTable);
}
