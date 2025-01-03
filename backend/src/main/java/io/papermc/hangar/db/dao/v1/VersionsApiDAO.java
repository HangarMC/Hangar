package io.papermc.hangar.db.dao.v1;

import io.papermc.hangar.db.extras.BindPagination;
import io.papermc.hangar.db.mappers.VersionStatsMapper;
import io.papermc.hangar.model.api.project.version.PluginDependency;
import io.papermc.hangar.model.api.project.version.Version;
import io.papermc.hangar.model.api.project.version.VersionStats;
import io.papermc.hangar.model.api.requests.RequestPagination;
import io.papermc.hangar.model.common.Platform;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.jdbi.v3.core.enums.EnumStrategy;
import org.jdbi.v3.spring.JdbiRepository;
import org.jdbi.v3.sqlobject.config.KeyColumn;
import org.jdbi.v3.sqlobject.config.RegisterColumnMapper;
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

    @KeyColumn("id")
    @RegisterColumnMapper(VersionStatsMapper.class)
    @SqlQuery("""
        SELECT pv.id,
               pv.created_at,
               pv.version_string,
               pv.visibility,
               pv.description,
               coalesce((SELECT sum(pvd.downloads) FROM project_versions_downloads pvd WHERE pv.id = pvd.version_id), 0) vs_totalDownloads,
               (select array_agg(d) from (SELECT pvd.platform, sum(pvd.downloads) FROM project_versions_downloads pvd WHERE pv.id = pvd.version_id GROUP BY pvd.platform) d) vs_platformDownloads,
               u.name author,
               pv.review_state,
               pc.created_at pc_created_at,
               pc.name pc_name,
               pc.description pc_description,
               pc.color pc_color,
               pc.flags pc_flags,
               CASE
                   WHEN exists(SELECT * FROM pinned_versions piv WHERE piv.version_id = pv.id AND lower(type) = 'channel') THEN 'CHANNEL'
                   WHEN exists(SELECT * FROM pinned_versions piv WHERE piv.version_id = pv.id AND lower(type) = 'version') THEN 'VERSION'
                   ELSE 'NONE'
               END AS pinnedStatus
           FROM project_versions pv
               JOIN project_channels pc ON pv.channel_id = pc.id
               LEFT JOIN users u ON pv.author_id = u.id
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
    @Nullable Map.Entry<Long, Version> getVersion(long versionId, @Define boolean canSeeHidden, @Define Long userId);

    @KeyColumn("id")
    @RegisterColumnMapper(VersionStatsMapper.class)
    @SqlQuery("""
        <if(platformfilter)>
        WITH sq AS (SELECT array_agg(DISTINCT plv.platform) platforms, array_agg(DISTINCT plv.version) versions, pvpd.version_id
                    FROM project_version_platform_dependencies pvpd
                             JOIN platform_versions plv ON pvpd.platform_version_id = plv.id
                    GROUP BY pvpd.version_id)
        <endif>
        SELECT pv.id,
               pv.created_at,
               pv.version_string,
               pv.visibility,
               pv.description,
               coalesce((SELECT sum(pvd.downloads) FROM project_versions_downloads pvd WHERE pv.id = pvd.version_id), 0) vs_totalDownloads,
               (select array_agg(d) from (SELECT pvd.platform, sum(pvd.downloads) FROM project_versions_downloads pvd WHERE pv.id = pvd.version_id GROUP BY pvd.platform) d) vs_platformDownloads,
               (select u.name from users u where u.id = pv.author_id) as author,
               pv.review_state,
               pc.created_at pc_created_at,
               pc.name pc_name,
               pc.description pc_description,
               pc.color pc_color,
               pc.flags pc_flags,
               CASE
                   WHEN exists(SELECT * FROM pinned_versions piv WHERE piv.version_id = pv.id AND lower(type) = 'channel') THEN 'CHANNEL'
                   WHEN exists(SELECT * FROM pinned_versions piv WHERE piv.version_id = pv.id AND lower(type) = 'version') THEN 'VERSION'
                   ELSE 'NONE'
               END AS pinnedStatus
           FROM project_versions pv
               JOIN project_channels pc ON pv.channel_id = pc.id
               <if(platformfilter)>INNER JOIN sq ON pv.id = sq.version_id<endif>
           WHERE TRUE <filters>
               <if(!canSeeHidden)>
                   AND (pv.visibility = 0
                   <if(userId)>
                       OR (<userId> IN (SELECT pm.user_id FROM project_members_all pm WHERE pm.id = :projectId) AND pv.visibility != 4)
                   <endif>)
               <endif>
               AND pv.project_id = :projectId
           ORDER BY pv.created_at DESC <offsetLimit>
    """)
    SortedMap<Long, Version> getVersions(long projectId, @Define boolean canSeeHidden, @Define Long userId, @BindPagination RequestPagination pagination);

    @SqlQuery("""
           <if(platformfilter)>
           WITH sq AS (SELECT array_agg(DISTINCT plv.platform) platforms, array_agg(DISTINCT plv.version) versions, pvpd.version_id
                       FROM project_version_platform_dependencies pvpd
                                JOIN platform_versions plv ON pvpd.platform_version_id = plv.id
                       GROUP BY pvpd.version_id)
           <endif>
           SELECT count(DISTINCT pv.id)
           FROM project_versions pv
                    JOIN project_channels pc ON pv.channel_id = pc.id
                    <if(platformfilter)>INNER JOIN sq ON pv.id = sq.version_id<endif>
           WHERE TRUE <filters>
               <if(!canSeeHidden)>
                   AND (pv.visibility = 0
                   <if(userId)>
                      OR (<userId> IN (SELECT pm.user_id FROM project_members_all pm WHERE pm.id = :projectId) AND pv.visibility != 4)
                   <endif>)
               <endif>
               AND pv.project_id = :projectId""")
    Long getVersionCount(long projectId, @Define boolean canSeeHidden, @Define Long userId, @BindPagination(isCount = true) RequestPagination pagination);

    @SqlQuery("SELECT " +
        "       pvd.name," +
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

    @SqlQuery("SELECT " +
        "       pvd.name," +
        "       pvd.required," +
        "       pvd.external_url," +
        "       p.owner_name pn_owner," +
        "       p.slug pn_slug," +
        "       pvd.platform" +
        "   FROM project_version_dependencies pvd" +
        "       LEFT JOIN projects p ON pvd.project_id = p.id" +
        "   WHERE pvd.version_id = :versionId")
    @RegisterConstructorMapper(PluginDependency.class)
    Set<PluginDependency> getPluginDependencies(long versionId);

    @KeyColumn("platform")
    @ValueColumn("versions")
    @SqlQuery("SELECT" +
        "       pv.platform," +
        "       array_agg(pv.version ORDER BY pv.created_at) versions" +
        "   FROM project_version_platform_dependencies pvpd " +
        "       JOIN platform_versions pv ON pvpd.platform_version_id = pv.id" +
        "   WHERE pvpd.version_id = :versionId" +
        "   GROUP BY pv.platform")
    Map<Platform, SortedSet<String>> getPlatformDependencies(long versionId);

    @KeyColumn("date")
    @RegisterConstructorMapper(value = VersionStats.class, prefix = "vs")
    @RegisterColumnMapper(VersionStatsMapper.class)
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
}
