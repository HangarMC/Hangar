package io.papermc.hangar.components.index;

import io.papermc.hangar.model.api.project.Project;
import io.papermc.hangar.model.api.project.version.Version;
import java.util.List;
import org.jdbi.v3.spring.JdbiRepository;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

@JdbiRepository
@RegisterConstructorMapper(Project.class)
@RegisterConstructorMapper(Version.class)
public interface IndexDAO {

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
        /* platforms todo maybe we dont need this and instead should optimize platforms into versions */
               (WITH aggregated_versions AS (SELECT plv.platform,
                                                    json_agg(DISTINCT plv.version) AS versions
                                             FROM project_versions pv
                                                 JOIN project_version_platform_dependencies pvpd ON pv.id = pvpd.version_id
                                                 JOIN platform_versions plv ON pvpd.platform_version_id = plv.id
                                             WHERE pv.project_id = p.id
                                             GROUP BY plv.platform)
                SELECT json_agg(
                           json_build_object('platform', av.platform, 'versions', av.versions)
                       ) AS platform_versions
                FROM aggregated_versions av)
        /* todo project memebers, for user profile page */
        FROM projects p
            JOIN project_stats_view ps on ps.id = p.id
        """)
    List<Project> getAllProjects();

    // TODO dont use view here
    @SqlQuery("SELECT * FROM version_view vv")
    List<Version> getAllVersions();
}
