package io.papermc.hangar.db.dao.api;

import io.papermc.hangar.db.dao.api.mappers.VersionMapper;
import io.papermc.hangar.db.mappers.VersionDependenciesMapper;
import io.papermc.hangar.model.generated.Version;
import io.papermc.hangar.model.generated.VersionStatsDay;
import org.jdbi.v3.sqlobject.config.KeyColumn;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.config.RegisterColumnMapper;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.BindList;
import org.jdbi.v3.sqlobject.customizer.BindList.EmptyHandling;
import org.jdbi.v3.sqlobject.customizer.Define;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.stringtemplate4.UseStringTemplateEngine;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Repository
@RegisterRowMapper(VersionMapper.class)
public interface VersionsApiDao {

    @UseStringTemplateEngine
    @RegisterColumnMapper(VersionDependenciesMapper.class)
    @SqlQuery("SELECT pv.id," +
            "pv.created_at," +
            "pv.version_string," +
            "pv.dependencies," +
            "pv.visibility," +
            "pv.description," +
            "coalesce((SELECT sum(pvd.downloads) FROM project_versions_downloads pvd WHERE p.id = pvd.project_id AND pv.id = pvd.version_id), 0) downloads," +
            "pv.file_size fi_size_bytes," +
            "pv.hash fi_md5_hash," +
            "pv.file_name fi_name," +
            "u.name author," +
            "pv.review_state," +
            "array_append(array_agg(pvt.name ORDER BY (pvt.name)) FILTER ( WHERE pvt.name IS NOT NULL ), 'Channel')  AS tag_name," +
            "array_append(array_agg(pvt.data ORDER BY (pvt.name)) FILTER ( WHERE pvt.name IS NOT NULL ), pc.name)    AS tag_data," +
            "array_append(array_agg(pvt.color ORDER BY (pvt.name)) FILTER ( WHERE pvt.name IS NOT NULL ), pc.color + 9) AS tag_color " +
            "FROM projects p" +
            "   JOIN project_versions pv ON p.id = pv.project_id" +
            "   LEFT JOIN users u ON pv.author_id = u.id" +
            "   LEFT JOIN project_version_tags pvt ON pv.id = pvt.version_id" +
            "   LEFT JOIN project_channels pc ON pv.channel_id = pc.id " +
            "WHERE <if(!canSeeHidden)>(pv.visibility = 0 " +
            "<if(userId)>OR (<userId> IN (SELECT pm.user_id FROM project_members_all pm WHERE pm.id = p.id) AND pv.visibility != 4) <endif>) AND <endif> " +
            "p.slug = :slug AND " +
            "p.owner_name = :author AND " +
            "pv.id = :versionId " +
            "GROUP BY p.id, pv.id, u.id, pc.id " +
            "ORDER BY pv.created_at DESC LIMIT 1")
    Version getVersion(String author, String slug, long versionId, @Define boolean canSeeHidden, @Define Long userId);

    @RegisterColumnMapper(VersionDependenciesMapper.class)
    @UseStringTemplateEngine
    @SqlQuery("SELECT pv.id," +
            "pv.created_at," +
            "pv.version_string," +
            "pv.dependencies," +
            "pv.visibility," +
            "pv.description," +
            "coalesce((SELECT sum(pvd.downloads) FROM project_versions_downloads pvd WHERE p.id = pvd.project_id AND pv.id = pvd.version_id), 0) downloads," +
            "pv.file_size fi_size_bytes," +
            "pv.hash fi_md5_hash," +
            "pv.file_name fi_name," +
            "u.name author," +
            "pv.review_state," +
            "array_append(array_agg(pvt.name ORDER BY (pvt.name)) FILTER ( WHERE pvt.name IS NOT NULL ), 'Channel')  AS tag_name," +
            "array_append(array_agg(array_to_string(pvt.data, ', ') ORDER BY (pvt.name)) FILTER ( WHERE pvt.name IS NOT NULL ), pc.name::text)    AS tag_data," +
            "array_append(array_agg(pvt.color ORDER BY (pvt.name)) FILTER ( WHERE pvt.name IS NOT NULL ), pc.color) AS tag_color " +
            "FROM projects p" +
            "   JOIN project_versions pv ON p.id = pv.project_id" +
            "   LEFT JOIN users u ON pv.author_id = u.id" +
            "   LEFT JOIN project_version_tags pvt ON pv.id = pvt.version_id" +
            "   LEFT JOIN project_channels pc ON pv.channel_id = pc.id " +
            "WHERE <if(!canSeeHidden)>(pv.visibility = 0 " +
            "<if(userId)>OR (<userId> IN (SELECT pm.user_id FROM project_members_all pm WHERE pm.id = p.id) AND pv.visibility != 4) <endif>) AND <endif> " +
            "p.slug = :slug AND " +
            "p.owner_name = :author <if(tags)> AND " +
            "(" +
            "   pvt.name || ':' || pvt.data IN (<tags>) OR " +
            "   pvt.name IN (<tags>) OR " +
            "   'Channel:' || pc.name IN (<tags>) OR " +
            "   'Channel' IN (<tags>)" +
            "  )<endif> " +
            "GROUP BY p.id, pv.id, u.id, pc.id " +
            "ORDER BY pv.created_at DESC LIMIT :limit OFFSET :offset")
    List<Version> listVersions(String author, String slug, @BindList(value = "tags", onEmpty = EmptyHandling.NULL_VALUE) List<String> tags, @Define boolean canSeeHidden, Long limit, long offset, @Define Long userId);

    @UseStringTemplateEngine
    @SqlQuery("SELECT COUNT(*) " +
            "FROM projects p" +
            "   JOIN project_versions pv ON p.id = pv.project_id" +
            "   LEFT JOIN users u ON pv.author_id = u.id" +
            "   LEFT JOIN project_version_tags pvt ON pv.id = pvt.version_id" +
            "   LEFT JOIN project_channels pc ON pv.channel_id = pc.id " +
            "WHERE <if(!canSeeHidden)>(pv.visibility = 0 " +
            "<if(userId)>OR (<userId> IN (SELECT pm.user_id FROM project_members_all pm WHERE pm.id = p.id) AND pv.visibility != 4) <endif>) AND <endif> " +
            "p.slug = :slug AND " +
            "p.owner_name = :author <if(tags)> AND " +
            "(" +
            "   pvt.name || ':' || pvt.data IN (<tags>) OR " +
            "   pvt.name IN (<tags>) OR " +
            "   'Channel:' || pc.name IN (<tags>) OR " +
            "   'Channel' IN (<tags>)" +
            "  )<endif> " +
            "GROUP BY p.id, pv.id, u.id, pc.id ")
    Long versionCount(String author, String slug, @BindList(value = "tags", onEmpty = EmptyHandling.NULL_VALUE) List<String> tags, @Define boolean canSeeHidden, @Define Long userId);

    @KeyColumn("date")
    @RegisterBeanMapper(value = VersionStatsDay.class, prefix = "vsd")
    @UseStringTemplateEngine
    @SqlQuery("SELECT CAST(dates.day as DATE) date, coalesce(pvd.downloads, 0) vsd_downloads" +
            "    FROM projects p," +
            "         project_versions pv," +
            "         (SELECT generate_series(:fromDate::DATE, :toDate::DATE, INTERVAL '1 DAY') AS day) dates" +
            "             LEFT JOIN project_versions_downloads pvd ON dates.day = pvd.day" +
            "    WHERE p.owner_name = :author" +
            "      AND pv.slug = :slug" +
            "      AND pv.id = :versionId" +
            "      AND (pvd IS NULL OR (pvd.project_id = p.id AND pvd.version_id = pv.id));")
    Map<String, VersionStatsDay> versionStats(String author, String slug, long versionId, LocalDate fromDate, LocalDate toDate);
}
