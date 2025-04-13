package io.papermc.hangar.components.index;

import io.papermc.hangar.model.api.project.Project;
import io.papermc.hangar.model.api.project.version.Version;
import java.util.List;
import org.jdbi.v3.spring.JdbiRepository;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Define;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.stringtemplate4.UseStringTemplateEngine;

@JdbiRepository
@RegisterConstructorMapper(Project.class)
@RegisterConstructorMapper(Version.class)
public interface IndexDAO {

    @UseStringTemplateEngine
    @SqlQuery("""
        SELECT p.id,
               p.created_at,
               p.name,
               p.owner_name                                                               AS owner,
               p.slug,
               p.category,
               p.description,
               p.sponsors,
               p.donation_enabled,
               p.donation_subject,
               p.keywords,
               p.license_name,
               p.license_url,
               p.license_type,
               p.visibility,
               p.links,
               p.tags,
            /* stats */
               ps.views,
               ps.downloads,
               ps.recent_views,
               ps.recent_downloads,
               ps.stars,
               ps.watchers,
               ps.last_updated,
            /* avatar stuff */
               (SELECT '/project/' || p.id || '.webp?v=' || a.version
                FROM avatars a
                WHERE a.type = 'project' AND a.subject = p.id::text)                      AS avatar,
               (SELECT '/user/' || o.uuid::text || '.webp?v=' || a.version
                FROM avatars a
                    JOIN users o ON a.subject = o.uuid::text
                WHERE o.id = p.owner_id AND a.type = 'user' AND a.subject = o.uuid::text) AS avatar_fallback,
            /* platforms */
               (SELECT jsonb_agg(jsonb_build_object('platform', platform, 'versions', versions)) AS platforms
                FROM (SELECT platformelement ->> 'platform'            AS platform,
                             json_agg(DISTINCT platformversionelement) AS versions
                      FROM project_versions pv,
                           LATERAL jsonb_array_elements(pv.platforms) AS platformelement,
                           LATERAL jsonb_array_elements(platformelement -> 'versions') AS platformversionelement
                      WHERE pv.project_id = p.id
                      GROUP BY platformelement ->> 'platform') sub)                       AS supported_platforms,
            /* main page */
               (SELECT pp.contents
                FROM project_pages pp
                WHERE pp.project_id = p.id AND pp.homepage = TRUE)                        AS main_page_content,
            /* members */
               (SELECT array_agg(lower(u.name))
                FROM project_members_all pma
                    JOIN users u ON pma.user_id = u.id
                WHERE pma.id = p.id)                                                      AS member_names
        FROM projects p
            JOIN project_stats_view ps ON ps.id = p.id
        <where>
        """)
    List<Project> getAllProjects(@Define String where);

    // TODO dont use view here <---
    @UseStringTemplateEngine
    @SqlQuery("SELECT * FROM version_view vv <where>")
    List<Version> getAllVersions(@Define String where);
}
