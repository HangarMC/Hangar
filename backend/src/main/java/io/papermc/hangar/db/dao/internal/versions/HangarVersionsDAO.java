package io.papermc.hangar.db.dao.internal.versions;

import io.papermc.hangar.model.internal.projects.HangarProject;
import java.util.List;
import org.jdbi.v3.core.enums.EnumStrategy;
import org.jdbi.v3.spring.JdbiRepository;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.config.UseEnumStrategy;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.stringtemplate4.UseStringTemplateEngine;

@JdbiRepository
@UseStringTemplateEngine
@UseEnumStrategy(EnumStrategy.BY_ORDINAL)
public interface HangarVersionsDAO {

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
