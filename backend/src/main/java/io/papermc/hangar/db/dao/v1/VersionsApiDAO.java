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
           SELECT * FROM version_view vv
           WHERE
               <if(!canSeeHidden)>
                   (vv.visibility = 0
                   <if(userId)>
                       OR (<userId> IN (SELECT pm.user_id FROM project_members_all pm WHERE pm.id = vv.project_id) AND vv.visibility != 4)
                   <endif>)
                   AND
               <endif>
               vv.id = :versionId
           ORDER BY vv.created_at DESC
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

    // TODO platform
    @SqlQuery("""
        SELECT pv.id
           FROM project_versions pv
               JOIN project_channels pc ON pv.channel_id = pc.id
           WHERE pv.visibility = 0 AND pc.name = :channel AND pv.project_id = :projectId
           ORDER BY pv.created_at DESC
           LIMIT 1
    """)
    Long getLatestVersionId(long projectId, @Nullable String channel, Platform platform);
}
