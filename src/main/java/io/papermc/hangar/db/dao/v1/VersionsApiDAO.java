package io.papermc.hangar.db.dao.v1;

import io.papermc.hangar.model.api.color.TagColor;
import io.papermc.hangar.model.api.project.version.Tag;
import io.papermc.hangar.model.api.project.version.Version;
import io.papermc.hangar.model.api.project.version.VersionStats;
import io.papermc.hangar.model.common.Color;
import io.papermc.hangar.util.StringUtils;
import org.jdbi.v3.core.result.LinkedHashMapRowReducer;
import org.jdbi.v3.core.result.RowView;
import org.jdbi.v3.sqlobject.config.KeyColumn;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.BindList;
import org.jdbi.v3.sqlobject.customizer.Define;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.UseRowReducer;
import org.jdbi.v3.stringtemplate4.UseStringTemplateEngine;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Repository
@UseStringTemplateEngine
@RegisterConstructorMapper(Version.class)
public interface VersionsApiDAO {

    @UseRowReducer(VersionsApiDAO.VersionTagReducer.class)
    @SqlQuery("SELECT pv.id, pv.created_at," +
            "pv.version_string," +
            "pv.version_string || '.' || pv.id AS url_path," +
            "pv.visibility," +
            "pv.description," +
            "coalesce((SELECT sum(pvd.downloads) FROM project_versions_downloads pvd WHERE p.id = pvd.project_id AND pv.id = pvd.version_id), 0) vs_downloads," +
            "pv.file_name fi_name," +
            "pv.file_size fi_size_bytes," +
            "pv.hash fi_md5_hash," +
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

    @UseRowReducer(VersionsApiDAO.VersionTagReducer.class)
    @SqlQuery("SELECT pv.id, pv.created_at," +
            "pv.version_string," +
            "pv.version_string || '.' || pv.id AS url_path," +
            "pv.visibility," +
            "pv.description," +
            "coalesce((SELECT sum(pvd.downloads) FROM project_versions_downloads pvd WHERE p.id = pvd.project_id AND pv.id = pvd.version_id), 0) vs_downloads," +
            "pv.file_name fi_name," +
            "pv.file_size fi_size_bytes," +
            "pv.hash fi_md5_hash," +
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
    List<Version> getVersions(String author, String slug, @BindList(onEmpty = BindList.EmptyHandling.NULL_VALUE) List<String> tags, @Define boolean canSeeHidden, @Define Long userId, long limit, long offset);

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
            "GROUP BY p.id, pv.id, u.id, pc.id")
    Long getVersionCount(String author, String slug, @BindList(onEmpty = BindList.EmptyHandling.NULL_VALUE) List<String> tags, @Define boolean canSeeHidden, @Define Long userId);

    @KeyColumn("date")
    @RegisterConstructorMapper(value = VersionStats.class, prefix = "vs")
    @SqlQuery("SELECT CAST(dates.day as DATE) date, coalesce(pvd.downloads, 0) vs_downloads" +
            "    FROM projects p," +
            "         project_versions pv," +
            "         (SELECT generate_series(:fromDate::DATE, :toDate::DATE, INTERVAL '1 DAY') AS day) dates" +
            "             LEFT JOIN project_versions_downloads pvd ON dates.day = pvd.day" +
            "    WHERE p.owner_name = :author" +
            "      AND p.slug = :slug" +
            "      AND pv.id = :versionId" +
            "      AND (pvd IS NULL OR (pvd.project_id = p.id AND pvd.version_id = pv.id));")
    Map<String, VersionStats> getVersionStats(String author, String slug, long versionId, OffsetDateTime fromDate, OffsetDateTime toDate);

    class VersionTagReducer implements LinkedHashMapRowReducer<Long, Version> {
        @Override
        public void accumulate(Map<Long, Version> container, RowView rowView) {
            final Version version = container.computeIfAbsent(rowView.getColumn("id", Long.class), id -> rowView.getRow(Version.class));

            String[] tagNames = rowView.getColumn("tag_name", String[].class);
            String[] tagData = rowView.getColumn("tag_data", String[].class);
            Integer[] tagColors = rowView.getColumn("tag_color", Integer[].class);
            if (tagNames.length != tagData.length || tagData.length != tagColors.length) {
                throw new IllegalArgumentException("All 3 tag arrays must be the same length");
            }

            List<Tag> tags = new ArrayList<>();
            for (int i = 0; i < tagNames.length; i++) {
                TagColor apiTagColor;
                io.papermc.hangar.model.common.TagColor tagColor = io.papermc.hangar.model.common.TagColor.getByName(tagNames[i]);
                if (tagColor == null) {
                    Color color = Color.getValues()[tagColors[i]];
                    apiTagColor = new TagColor(null, color.getHex());
                } else {
                    apiTagColor = tagColor.toTagColor();
                }
                String data = StringUtils.formatVersionNumbers(Arrays.asList(tagData[i].split(", ")));

                tags.add(new Tag(tagNames[i], data, apiTagColor));
            }
            version.getTags().addAll(tags);
        }
    }
}
