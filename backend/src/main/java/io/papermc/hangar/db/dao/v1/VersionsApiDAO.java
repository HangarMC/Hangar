package io.papermc.hangar.db.dao.v1;

import io.papermc.hangar.model.api.project.version.PluginDependency;
import io.papermc.hangar.model.api.project.version.Version;
import io.papermc.hangar.model.api.project.version.VersionStats;
import io.papermc.hangar.model.common.Platform;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.jdbi.v3.core.enums.EnumStrategy;
import org.jdbi.v3.spring.JdbiRepository;
import org.jdbi.v3.sqlobject.config.KeyColumn;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.config.UseEnumStrategy;
import org.jdbi.v3.sqlobject.config.ValueColumn;
import org.jdbi.v3.sqlobject.customizer.Define;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.stringtemplate4.UseStringTemplateEngine;
import org.jetbrains.annotations.Nullable;

@JdbiRepository
@UseStringTemplateEngine
@UseEnumStrategy(EnumStrategy.BY_ORDINAL)
@RegisterConstructorMapper(Version.class)
public interface VersionsApiDAO {

    @SqlQuery("""
     SELECT pv.id,
            pv.created_at,
            pv.version_string,
            pv.visibility,
            pv.description,
            pv.project_id,
            coalesce(vsv.totaldownloads, 0)                        AS vs_totaldownloads,
            vsv.platformdownloads                                  AS vs_platformdownloads,
            (SELECT u.name FROM users u WHERE u.id = pv.author_id) AS author,
            pv.review_state,
            pc.created_at                                          AS pc_created_at,
            pc.name                                                AS pc_name,
            pc.description                                         AS pc_description,
            pc.color                                               AS pc_color,
            pc.flags                                               AS pc_flags,
            CASE
                WHEN exists(SELECT * FROM pinned_versions piv WHERE piv.version_id = pv.id AND lower(type) = 'channel')
                    THEN 'CHANNEL'
                WHEN exists(SELECT * FROM pinned_versions piv WHERE piv.version_id = pv.id AND lower(type) = 'version')
                    THEN 'VERSION'
                ELSE 'NONE'
                END                                                AS pinnedstatus,
            (SELECT ARRAY [p.owner_name, p.slug]
             FROM projects p
             WHERE p.id = pv.project_id
             LIMIT 1)                                              AS project_namespace,-- needed for downloads
            (SELECT json_agg(json_build_object('file_size', file_size,
                                               'hash', hash,
                                               'file_name', file_name,
                                               'external_url', external_url,
                                               'platforms', platforms,
                                               'download_platform', download_platform)) AS value
             FROM project_version_downloads
             WHERE version_id = pv.id
             GROUP BY version_id)                                  AS downloads,
            (SELECT json_agg(json_build_object('name', pvd.name,
                                               'project_id', pvd.project_id,
                                               'required', pvd.required,
                                               'external_url', pvd.external_url,
                                               'platform', pvd.platform)) AS value
             FROM project_version_dependencies pvd
             WHERE pvd.version_id = pv.id)                         AS plugin_dependencies,
            pv.platforms                                           AS platform_dependencies,
            'dum'                                                  AS platform_dependencies_formatted
     FROM project_versions pv
         JOIN project_channels pc ON pv.channel_id = pc.id
         LEFT JOIN version_stats_view vsv ON pv.id = vsv.id
     WHERE
         <if(!canSeeHidden)>
             (pv.visibility = 0
             <if(userId)>
                 OR (<userId> IN (SELECT pm.user_id FROM project_members_all pm WHERE pm.id = pv.project_id) AND pv.visibility != 4)
             <endif>)
             AND
         <endif>
         pv.id = :versionId
     ORDER BY pv.created_at DESC
    """)
    @Nullable Version getVersion(long versionId, @Define boolean canSeeHidden, @Define Long userId);

    @SqlQuery("SELECT " +
        "       pvd.name," +
        "       pvd.project_id," +
        "       pvd.required," +
        "       pvd.external_url," +
        "       pvd.platform," +
        "       p.owner_name pn_owner," +
        "       p.slug pn_slug" +
        "   FROM project_version_dependencies pvd" +
        "       LEFT JOIN projects p ON pvd.project_id = p.id" +
        "   WHERE pvd.version_id = :versionId AND pvd.platform = :platform")
    @RegisterConstructorMapper(PluginDependency.class)
    Set<PluginDependency> getPluginDependencies(long versionId, @EnumByOrdinal Platform platform); // TODO make into one db call for all platforms?

    @KeyColumn("date")
    @RegisterConstructorMapper(value = VersionStats.class, prefix = "vs")
    @SqlQuery("""
        SELECT date, sum(platform_downloads) vs_totalDownloads, array_agg((ARRAY[subquery.platform, subquery.platform_downloads])) AS vs_platformDownloads
        FROM (SELECT cast(dates.day AS date) date,
                     pvd.platform            platform,
                     coalesce(sum(pvd.downloads), 0) AS   platform_downloads
              FROM projects p,
                   project_versions pv,
                   (SELECT generate_series(:fromDate::date, :toDate::date, INTERVAL '1 DAY') AS day) dates
                       LEFT JOIN project_versions_downloads pvd ON dates.day = pvd.day
              WHERE p.id = :projectId
                AND pv.id = :versionId
                AND (pvd IS NULL OR (pvd.project_id = p.id AND pvd.version_id = pv.id))
              GROUP BY date, platform) subquery
        GROUP BY date;
        """)
    Map<String, VersionStats> getVersionStats(long projectId, long versionId, OffsetDateTime fromDate, OffsetDateTime toDate);

    @SqlQuery("""
        SELECT pv.version_string
           FROM project_versions pv
               JOIN project_channels pc ON pv.channel_id = pc.id
           WHERE pv.visibility = 0 AND pc.name = :channel AND pv.project_id = :projectId
           ORDER BY pv.created_at DESC
           LIMIT 1
    """)
    @Nullable String getLatestVersion(long projectId, String channel);

    @SqlQuery("""
        SELECT pv.id
           FROM project_versions pv
               JOIN project_channels pc ON pv.channel_id = pc.id
           WHERE pv.visibility = 0
             AND (:channel IS NULL OR pc.name = :channel)
             AND pv.project_id = :projectId
             AND EXISTS (
                 SELECT 1 FROM jsonb_array_elements(pv.platforms) elem WHERE (elem->>'platform')::int = :platform
             )
           ORDER BY pv.created_at DESC
           LIMIT 1
    """)
    Long getLatestVersionId(long projectId, @Nullable String channel, @EnumByOrdinal Platform platform);
}
