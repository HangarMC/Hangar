package io.papermc.hangar.db.dao.internal.versions;

import io.papermc.hangar.model.internal.projects.HangarProject;
import java.util.List;
import org.jdbi.v3.core.enums.EnumStrategy;
import org.jdbi.v3.spring.JdbiRepository;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.config.UseEnumStrategy;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.stringtemplate4.UseStringTemplateEngine;

@JdbiRepository
@UseStringTemplateEngine
@UseEnumStrategy(EnumStrategy.BY_ORDINAL)
public interface HangarVersionsDAO {

    // TODO fixup view and this
    // TODO use the version view here
    @SqlQuery("""
        (SELECT pv.version_id,
                pv.version_string,
                pv."type",
                pv.version_string                                                                        AS name,
                pc.name                                                                                     pc_name,
                pc.description                                                                              pc_description,
                pc.created_at                                                                               pc_created_at,
                pc.color                                                                                    pc_color,
                pc.flags                                                                                    pc_flags,
                (SELECT ARRAY [p.owner_name, p.slug] FROM projects p WHERE p.id = pv.project_id LIMIT 1) AS project_namespace,-- needed for downloads
                (SELECT json_agg(json_build_object('file_size', pvd.file_size,
                                                   'hash', pvd.hash,
                                                   'file_name', pvd.file_name,
                                                   'external_url', pvd.external_url,
                                                   'platforms', pvd.platforms,
                                                   'download_platform', pvd.download_platform)) AS value
                 FROM project_version_downloads pvd
                 WHERE pvd.version_id = pv.version_id
                 GROUP BY pvd.version_id)                                                                AS downloads,
                pv.platforms as platform_dependencies,
                'dum'                                                                                    AS platform_dependencies_formatted
         FROM pinned_versions pv
             JOIN project_versions p ON pv.version_id = p.id
             JOIN project_channels pc ON pc.id = p.channel_id
         WHERE pv.project_id = :projectId AND pv.type = 'version' AND p.visibility = 0
         LIMIT 5)
        UNION ALL
        (SELECT pv.version_id,
                pv.version_string,
                pv."type",
                pv.version_string                                                                        AS name,
                pc.name                                                                                     pc_name,
                pc.description                                                                              pc_description,
                pc.created_at                                                                               pc_created_at,
                pc.color                                                                                    pc_color,
                pc.flags                                                                                    pc_flags,
                (SELECT ARRAY [p.owner_name, p.slug] FROM projects p WHERE p.id = pv.project_id LIMIT 1) AS project_namespace,-- needed for downloads
                (SELECT json_agg(json_build_object('file_size', pvd.file_size,
                                                   'hash', pvd.hash,
                                                   'file_name', pvd.file_name,
                                                   'external_url', pvd.external_url,
                                                   'platforms', pvd.platforms,
                                                   'download_platform', pvd.download_platform)) AS value
                 FROM project_version_downloads pvd
                 WHERE pvd.version_id = pv.version_id
                 GROUP BY pvd.version_id)                                                                AS downloads,
                 pv.platforms as platform_dependencies,
                'dum'                                                                                    AS platform_dependencies_formatted
         FROM pinned_versions pv
             JOIN project_versions p ON pv.version_id = p.id
             JOIN project_channels pc ON pc.id = p.channel_id
         WHERE pv.project_id = :projectId AND pv.type = 'channel' AND p.visibility = 0
         LIMIT 1)
        """)
    @RegisterConstructorMapper(HangarProject.PinnedVersion.class)
    List<HangarProject.PinnedVersion> getPinnedVersions(long projectId);
}
