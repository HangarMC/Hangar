package io.papermc.hangar.db.dao.v1;

import io.papermc.hangar.db.extras.BindPagination;
import io.papermc.hangar.db.mappers.TagMapper;
import io.papermc.hangar.model.api.project.version.PluginDependency;
import io.papermc.hangar.model.api.project.version.Tag;
import io.papermc.hangar.model.api.project.version.Version;
import io.papermc.hangar.model.api.project.version.VersionStats;
import io.papermc.hangar.model.api.requests.RequestPagination;
import io.papermc.hangar.model.common.Platform;
import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.jdbi.v3.core.enums.EnumStrategy;
import org.jdbi.v3.sqlobject.config.KeyColumn;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.config.UseEnumStrategy;
import org.jdbi.v3.sqlobject.config.ValueColumn;
import org.jdbi.v3.sqlobject.customizer.Define;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.stringtemplate4.UseStringTemplateEngine;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

@Repository
@UseStringTemplateEngine
@UseEnumStrategy(EnumStrategy.BY_ORDINAL)
@RegisterConstructorMapper(Version.class)
public interface VersionsApiDAO {

    @KeyColumn("id")
    @SqlQuery("SELECT pv.id," +
            "       pv.created_at," +
            "       pv.version_string," +
            "       pv.visibility," +
            "       pv.description," +
            "       coalesce((SELECT sum(pvd.downloads) FROM project_versions_downloads pvd WHERE p.id = pvd.project_id AND pv.id = pvd.version_id), 0) vs_downloads," +
            "       pv.file_name fi_name," +
            "       pv.file_size fi_size_bytes," +
            "       pv.hash fi_md5_hash," +
            "       pv.external_url," +
            "       u.name author," +
            "       pv.review_state," +
            "       exists(SELECT 1 FROM recommended_project_versions rpv WHERE rpv.version_id = pv.id) as recommended" +
            "   FROM project_versions pv" +
            "       JOIN projects p ON pv.project_id = p.id" +
            "       LEFT JOIN users u ON pv.author_id = u.id" +
            "   WHERE " +
            "       <if(!canSeeHidden)>" +
            "           (pv.visibility = 0 " +
            "           <if(userId)>" +
            "               OR (<userId> IN (SELECT pm.user_id FROM project_members_all pm WHERE pm.id = p.id) AND pv.visibility != 4) " +
            "           <endif>)" +
            "           AND" +
            "       <endif>" +
            "       pv.id = :versionId" +
            "   ORDERED BY pv.created_at DESC"
    )
    Entry<Long, Version> getVersion(long versionId, @Define boolean canSeeHidden, @Define Long userId);

    @KeyColumn("id")
    @SqlQuery("SELECT pv.id," +
            "       pv.created_at," +
            "       pv.version_string," +
            "       pv.visibility," +
            "       pv.description," +
            "       coalesce((SELECT sum(pvd.downloads) FROM project_versions_downloads pvd WHERE p.id = pvd.project_id AND pv.id = pvd.version_id), 0) vs_downloads," +
            "       pv.file_name fi_name," +
            "       pv.file_size fi_size_bytes," +
            "       pv.hash fi_md5_hash," +
            "       pv.external_url," +
            "       u.name author," +
            "       pv.review_state," +
            "       exists(SELECT 1 FROM recommended_project_versions rpv WHERE rpv.version_id = pv.id) as recommended" +
            "   FROM project_versions pv" +
            "       JOIN projects p ON pv.project_id = p.id" +
            "       LEFT JOIN users u ON pv.author_id = u.id" +
            "   WHERE " +
            "       <if(!canSeeHidden)>" +
            "           (pv.visibility = 0 " +
            "           <if(userId)>" +
            "               OR (<userId> IN (SELECT pm.user_id FROM project_members_all pm WHERE pm.id = p.id) AND pv.visibility != 4) " +
            "           <endif>)" +
            "           AND" +
            "       <endif>" +
            "       lower(p.owner_name) = lower(:author) AND" +
            "       lower(p.slug) = lower(:slug) AND" +
            "       pv.version_string = :versionString" +
            "   ORDER BY pv.created_at DESC"
    )
    SortedMap<Long, Version> getVersionsWithVersionString(String author, String slug, String versionString, @Define boolean canSeeHidden, @Define Long userId);

    @KeyColumn("id")
    @SqlQuery("SELECT pv.id," +
            "       pv.created_at," +
            "       pv.version_string," +
            "       pv.visibility," +
            "       pv.description," +
            "       coalesce((SELECT sum(pvd.downloads) FROM project_versions_downloads pvd WHERE p.id = pvd.project_id AND pv.id = pvd.version_id), 0) vs_downloads," +
            "       pv.file_name fi_name," +
            "       pv.file_size fi_size_bytes," +
            "       pv.hash fi_md5_hash," +
            "       pv.external_url," +
            "       u.name author," +
            "       pv.review_state," +
            "       exists(SELECT 1 FROM recommended_project_versions rpv WHERE rpv.version_id = pv.id) as recommended" +
            "   FROM project_versions pv" +
            "       JOIN projects p ON pv.project_id = p.id" +
            "       JOIN project_channels pc ON pv.channel_id = pc.id" +
            "       LEFT JOIN users u ON pv.author_id = u.id" +
            "       INNER JOIN (SELECT array_agg(DISTINCT plv.platform) platforms, pvpd.version_id" +
            "           FROM project_version_platform_dependencies pvpd" +
            "               JOIN platform_versions plv ON pvpd.platform_version_id = plv.id" +
            "           GROUP BY pvpd.version_id" +
            "       ) sq ON pv.id = sq.version_id" +
            "       INNER JOIN (SELECT pvt.name, pvt.data, pvt.version_id FROM project_version_tags pvt) vtsq ON pv.id = vtsq.version_id" +
            "   WHERE TRUE <filters>" +
            "       <if(!canSeeHidden)>" +
            "           AND (pv.visibility = 0 " +
            "           <if(userId)>" +
            "               OR (<userId> IN (SELECT pm.user_id FROM project_members_all pm WHERE pm.id = p.id) AND pv.visibility != 4) " +
            "           <endif>)" +
            "       <endif>" +
            "       AND lower(p.owner_name) = lower(:author) AND" +
            "       lower(p.slug) = lower(:slug) " +
            " GROUP BY pv.id, p.id, u.name, pv.created_at ORDER BY pv.created_at DESC <offsetLimit>")
    SortedMap<Long, Version> getVersions(String author, String slug, @Define boolean canSeeHidden, @Define Long userId, @BindPagination RequestPagination pagination);

    @SqlQuery("SELECT COUNT(DISTINCT pv.id)" +
            "   FROM project_versions pv" +
            "       JOIN projects p ON pv.project_id = p.id" +
            "       JOIN project_channels pc ON pv.channel_id = pc.id" +
            "       INNER JOIN (SELECT array_agg(DISTINCT plv.platform) platforms, pvpd.version_id" +
            "           FROM project_version_platform_dependencies pvpd" +
            "               JOIN platform_versions plv ON pvpd.platform_version_id = plv.id" +
            "           GROUP BY pvpd.version_id" +
            "       ) sq ON pv.id = sq.version_id" +
            "       INNER JOIN (SELECT pvt.name, pvt.data, pvt.version_id FROM project_version_tags pvt) vtsq ON pv.id = vtsq.version_id" +
            "   WHERE TRUE <filters> " +
            "       <if(!canSeeHidden)>" +
            "           AND (pv.visibility = 0 " +
            "           <if(userId)>" +
            "              OR (<userId> IN (SELECT pm.user_id FROM project_members_all pm WHERE pm.id = p.id) AND pv.visibility != 4)" +
            "           <endif>)" +
            "       <endif> " +
            "   AND lower(p.slug) = lower(:slug) AND " +
            "   lower(p.owner_name) = lower(:author)")
    Long getVersionCount(String author, String slug, @Define boolean canSeeHidden, @Define Long userId, @BindPagination(isCount = true) RequestPagination pagination);

    @KeyColumn("platform")
    @SqlQuery("SELECT " +
            "       pvd.name," +
            "       pvd.required," +
            "       pvd.external_url," +
            "       p.owner_name pn_owner," +
            "       p.slug pn_slug" +
            "   FROM project_version_dependencies pvd" +
            "       LEFT JOIN projects p ON pvd.project_id = p.id" +
            "   WHERE pvd.version_id = :versionId AND pvd.platform = :platform")
    @RegisterConstructorMapper(PluginDependency.class)
    Set<PluginDependency> getPluginDependencies(long versionId, @EnumByOrdinal Platform platform);

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

    @SqlQuery("SELECT pvt.name, pvt.data, pvt.color FROM project_version_tags pvt WHERE pvt.version_id = :versionId")
    @RegisterRowMapper(TagMapper.class)
    Set<Tag> getVersionTags(long versionId);

    // TODO this might be totally screwed up by adding the platform check
    @KeyColumn("date")
    @RegisterConstructorMapper(value = VersionStats.class, prefix = "vs")
    @SqlQuery("SELECT CAST(dates.day as DATE) date, coalesce(pvd.downloads, 0) vs_downloads" +
            "    FROM projects p," +
            "         project_versions pv" +
            "           JOIN project_version_platform_dependencies pvpd ON pv.id = pvpd.version_id" +
            "           JOIN platform_versions plv ON pvpd.platform_version_id = plv.id," +
            "         (SELECT generate_series(:fromDate::DATE, :toDate::DATE, INTERVAL '1 DAY') AS day) dates" +
            "             LEFT JOIN project_versions_downloads pvd ON dates.day = pvd.day" +
            "    WHERE p.owner_name = :author" +
            "      AND p.slug = :slug" +
            "      AND pv.version_string = :versionString" +
            "      AND plv.platform = :platform" +
            "      AND (pvd IS NULL OR (pvd.project_id = p.id AND pvd.version_id = pv.id));")
    Map<String, VersionStats> getVersionStats(String author, String slug, String versionString, @EnumByOrdinal Platform platform, OffsetDateTime fromDate, OffsetDateTime toDate);
}
