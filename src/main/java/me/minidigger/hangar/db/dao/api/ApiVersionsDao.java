package me.minidigger.hangar.db.dao.api;

import me.minidigger.hangar.db.dao.api.mappers.VersionMapper;
import me.minidigger.hangar.model.generated.Version;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.BindList;
import org.jdbi.v3.sqlobject.customizer.BindList.EmptyHandling;
import org.jdbi.v3.sqlobject.customizer.Define;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.stringtemplate4.UseStringTemplateEngine;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RegisterBeanMapper(Version.class)
public interface ApiVersionsDao {

    @UseStringTemplateEngine
    @RegisterRowMapper(VersionMapper.class)
    @SqlQuery("SELECT pv.created_at," +
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
              "<if(userId)>OR (:userId IN (SELECT pm.user_id FROM project_members_all pm WHERE pm.id = p.id) AND pv.visibility != 4) <endif>) AND <endif> " +
              "p.plugin_id = :pluginId <if(versionName)> AND " +
              " pv.version_string = '<versionName>' <endif> <if(tags)> AND " +
              "(" +
              "   pvt.name || ':' || pvt.data IN (<tags>) OR " +
              "   pvt.name IN (<tags>) OR " +
              "   'Channel:' || pc.name IN (<tags>) OR " +
              "   'Channel' IN (<tags>)" +
              "  )<endif> " +
              "GROUP BY p.id, pv.id, u.id, pc.id " +
              "ORDER BY pv.created_at DESC LIMIT :limit OFFSET :offset")
    List<Version> listVersions(String pluginId, @Define String versionName, @BindList(value = "tags", onEmpty = EmptyHandling.NULL_VALUE) List<String> tags, @Define boolean canSeeHidden, Long limit, long offset, @Define Long userId);
}
