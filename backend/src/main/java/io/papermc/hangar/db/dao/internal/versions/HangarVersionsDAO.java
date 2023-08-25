package io.papermc.hangar.db.dao.internal.versions;

import io.papermc.hangar.db.mappers.VersionStatsMapper;
import io.papermc.hangar.model.internal.projects.HangarProject;
import io.papermc.hangar.model.internal.versions.HangarVersion;
import java.util.List;
import org.jdbi.v3.core.enums.EnumStrategy;
import org.jdbi.v3.sqlobject.config.RegisterColumnMapper;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.config.UseEnumStrategy;
import org.jdbi.v3.sqlobject.customizer.Define;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.stringtemplate4.UseStringTemplateEngine;
import org.springframework.stereotype.Repository;

@Repository
@UseStringTemplateEngine
@UseEnumStrategy(EnumStrategy.BY_ORDINAL)
@RegisterConstructorMapper(HangarVersion.class)
public interface HangarVersionsDAO {

    @RegisterColumnMapper(VersionStatsMapper.class)
    @SqlQuery("""
        SELECT pv.id,
               pv.created_at,
               pv.version_string,
               pv.visibility,
               pv.description,
               coalesce((SELECT sum(pvd.downloads) FROM project_versions_downloads pvd WHERE p.id = pvd.project_id AND pv.id = pvd.version_id), 0) vs_totalDownloads,
               (select array_agg(d) from (SELECT pvd.platform, sum(pvd.downloads) FROM project_versions_downloads pvd WHERE p.id = pvd.project_id AND pv.id = pvd.version_id GROUP BY pvd.platform) d) vs_platformDownloads,
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
               END AS pinnedStatus,
               ru.name approved_by
           FROM project_versions pv
               JOIN project_channels pc ON pv.channel_id = pc.id
               JOIN projects p ON pv.project_id = p.id
               LEFT JOIN users u ON pv.author_id = u.id
               LEFT JOIN users ru ON pv.reviewer_id = ru.id
           WHERE
               <if(!canSeeHidden)>
                   (pv.visibility = 0
                   <if(userId)>
                       OR (<userId> IN (SELECT pm.user_id FROM project_members_all pm WHERE pm.id = p.id) AND pv.visibility != 4)
                   <endif>)
                   AND
               <endif>
               pv.id = :versionId
           ORDER BY pv.created_at DESC
        """)
    HangarVersion getVersion(long versionId, @Define boolean canSeeHidden, @Define Long userId);

    @RegisterColumnMapper(VersionStatsMapper.class)
    @SqlQuery("""
        SELECT pv.id,
               pv.created_at,
               pv.version_string,
               pv.visibility,
               pv.description,
               coalesce((SELECT sum(pvd.downloads) FROM project_versions_downloads pvd WHERE p.id = pvd.project_id AND pv.id = pvd.version_id), 0) vs_totalDownloads,
               (select array_agg(d) from (SELECT pvd.platform, sum(pvd.downloads) FROM project_versions_downloads pvd WHERE p.id = pvd.project_id AND pv.id = pvd.version_id GROUP BY pvd.platform) d) vs_platformDownloads,
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
               END AS pinnedStatus,
               ru.name approved_by
           FROM project_versions pv
               JOIN project_channels pc ON pv.channel_id = pc.id
               JOIN projects p ON pv.project_id = p.id
               LEFT JOIN users u ON pv.author_id = u.id
               LEFT JOIN users ru ON pv.reviewer_id = ru.id
           WHERE
               <if(!canSeeHidden)>
                   (pv.visibility = 0
                   <if(userId)>
                       OR (<userId> IN (SELECT pm.user_id FROM project_members_all pm WHERE pm.id = p.id) AND pv.visibility != 4)
                   <endif>)
                   AND
               <endif>
               lower(p.slug) = lower(:slug) AND
               pv.version_string = :versionString
        """)
    HangarVersion getVersionWithVersionString(String slug, String versionString, @Define boolean canSeeHidden, @Define Long userId);

    // TODO fixup view and this
    @SqlQuery("""
        (SELECT pv.version_id,
               pv."type",
               pv.version_string AS name,
               pc.name pc_name,
               pc.description pc_description,
               pc.created_at pc_created_at,
               pc.color pc_color,
               pc.flags pc_flags
        FROM pinned_versions pv
            JOIN project_versions p ON pv.version_id = p.id
            JOIN project_channels pc ON pc.id = p.channel_id
        WHERE pv.project_id = :projectId AND pv.type = 'version' AND p.visibility = 0 LIMIT 5)
        UNION ALL
        (SELECT pv.version_id,
               pv."type",
               pv.version_string AS name,
               pc.name pc_name,
               pc.description pc_description,
               pc.created_at pc_created_at,
               pc.color pc_color,
               pc.flags pc_flags
        FROM pinned_versions pv
            JOIN project_versions p ON pv.version_id = p.id
            JOIN project_channels pc ON pc.id = p.channel_id
        WHERE pv.project_id = :projectId AND pv.type = 'channel' AND p.visibility = 0 LIMIT 1)
        """)
    @RegisterConstructorMapper(HangarProject.PinnedVersion.class)
    List<HangarProject.PinnedVersion> getPinnedVersions(long projectId);
}
