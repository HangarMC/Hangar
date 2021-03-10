package io.papermc.hangar.db.dao.internal;

import io.papermc.hangar.db.dao.v1.VersionsApiDAO.VersionReducer;
import io.papermc.hangar.model.api.project.version.PluginDependency;
import io.papermc.hangar.model.internal.versions.HangarVersion;
import org.jdbi.v3.core.enums.EnumStrategy;
import org.jdbi.v3.core.result.LinkedHashMapRowReducer;
import org.jdbi.v3.core.result.RowView;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.config.UseEnumStrategy;
import org.jdbi.v3.sqlobject.customizer.Define;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.UseRowReducer;
import org.jdbi.v3.stringtemplate4.UseStringTemplateEngine;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
@RegisterConstructorMapper(HangarVersion.class)
public interface HangarVersionsDAO {
    @UseEnumStrategy(EnumStrategy.BY_ORDINAL)
    @UseRowReducer(HangarVersionReducer.class)
    @RegisterConstructorMapper(value = PluginDependency.class, prefix = "pd_")
    @UseStringTemplateEngine
    @SqlQuery("SELECT pv.id," +
            "         pv.created_at," +
            "         pv.version_string," +
            "         pv.visibility," +
            "         pv.description," +
            "         coalesce((SELECT sum(pvd.downloads) FROM project_versions_downloads pvd WHERE p.id = pvd.project_id AND pv.id = pvd.version_id), 0) vs_downloads," +
            "         pv.file_name fi_name," +
            "         pv.file_size fi_size_bytes," +
            "         pv.hash fi_md5_hash," +
            "         pv.external_url," +
            "         u.name author," +
            "         pv.review_state," +
            "         pvt.name AS tag_name," +
            "         pvt.data AS tag_data," +
            "         pvt.color AS tag_color," +
            "         'Channel' AS ch_tag_name," +
            "         pc.name AS ch_tag_data," +
            "         pc.color AS ch_tag_color," +
            "         d.platform pd_platform," +
            "         d.name pd_name," +
            "         d.required pd_required," +
            "         d.project_id pd_project_id," +
            "         d.external_url pd_external_url," +
            "         plv.platform p_platform," +
            "         plv.version p_version," +
            "         exists(SELECT 1 FROM recommended_project_versions rpv WHERE rpv.version_id = pv.id) as recommended," +
            "         ru.name approved_by" +
            "   FROM project_versions pv" +
            "       JOIN projects p ON pv.project_id = p.id" +
            "       LEFT JOIN users u ON pv.author_id = u.id" +
            "       LEFT JOIN project_version_tags pvt ON pv.id = pvt.version_id" +
            "       LEFT JOIN project_channels pc ON pv.channel_id = pc.id " +
            "       JOIN project_version_platform_dependencies pvpd ON pv.id = pvpd.version_id" +
            "       JOIN platform_versions plv ON pvpd.platform_version_id = plv.id" +
            "       LEFT JOIN project_version_dependencies d ON pv.id = d.version_id" +
            "       LEFT JOIN users ru ON pv.reviewer_id = ru.id" +
            "   WHERE <if(!canSeeHidden)>(pv.visibility = 0 " +
            "       <if(userId)>OR (<userId> IN (SELECT pm.user_id FROM project_members_all pm WHERE pm.id = p.id) AND pv.visibility != 4) <endif>) AND <endif> " +
            "       pvt.name IS NOT NULL AND" +
            "       lower(p.owner_name) = lower(:author) AND" +
            "       lower(p.slug) = lower(:slug) AND" +
            "       lower(pv.version_string) = lower(:versionString)" +
            "   GROUP BY p.id, pv.id, u.id, pc.id, d.id, plv.id, pvt.id, ru.id" +
            "   ORDER BY pv.created_at DESC")
    List<HangarVersion> getVersions(String author, String slug, String versionString, @Define boolean canSeeHidden, @Define Long userId);

    class HangarVersionReducer implements LinkedHashMapRowReducer<Long, HangarVersion> {
        @Override
        public void accumulate(Map<Long, HangarVersion> container, RowView rowView) {
            final HangarVersion version = container.computeIfAbsent(rowView.getColumn("id", Long.class), id -> rowView.getRow(HangarVersion.class));
            VersionReducer._accumulateVersion(rowView, version.getPluginDependencies(), version.getPlatformDependencies(), version.getTags(), version);
        }
    }
}
