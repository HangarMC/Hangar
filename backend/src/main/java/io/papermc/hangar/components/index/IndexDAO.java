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
               coalesce(ps.views, 0)                                                      AS views,
               coalesce(ps.downloads, 0)                                                  AS downloads,
               coalesce(ps.recent_views, 0)                                               AS recent_views,
               coalesce(ps.recent_downloads, 0)                                           AS recent_downloads,
               coalesce(ps.stars, 0)                                                      AS stars,
               coalesce(ps.watchers, 0)                                                   AS watchers,
               coalesce(ps.last_updated, now())                                           AS last_updated,
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
            LEFT JOIN project_stats_view ps ON ps.id = p.id
        <where>
        """)
    List<Project> getAllProjects(@Define String where);

    @UseStringTemplateEngine
    @SqlQuery("""
        SELECT pv.id,
               pv.created_at,
               pv.version_string,
               pv.visibility,
               pv.description,
               pv.project_id,
               coalesce(vsv.totaldownloads, 0)                        AS vs_totaldownloads,
               vsv.platformdownloads                                  AS vs_platformdownloads,
               (SELECT u.name FROM users u WHERE u.id = pv.author_id) AS author,
               pv.review_state,
               pc.created_at                                          AS pc_created_at,
               pc.name                                                AS pc_name,
               pc.description                                         AS pc_description,
               pc.color                                               AS pc_color,
               pc.flags                                               AS pc_flags,
            /* pinned */
               CASE
                   WHEN exists(SELECT * FROM pinned_versions piv WHERE piv.version_id = pv.id AND lower(type) = 'channel')
                       THEN 'CHANNEL'
                   WHEN exists(SELECT * FROM pinned_versions piv WHERE piv.version_id = pv.id AND lower(type) = 'version')
                       THEN 'VERSION'
                   ELSE 'NONE'
                   END                                                AS pinnedstatus,
            /* namespace */
               (SELECT ARRAY [p.owner_name, p.slug]
                FROM projects p
                WHERE p.id = pv.project_id
                LIMIT 1)                                              AS project_namespace,-- needed for downloads
            /* downloads */
               (SELECT json_agg(json_build_object('file_size', file_size,
                                                  'hash', hash,
                                                  'file_name', file_name,
                                                  'external_url', external_url,
                                                  'platforms', platforms,
                                                  'download_platform', download_platform)) AS value
                FROM project_version_downloads
                WHERE version_id = pv.id
                GROUP BY version_id)                                  AS downloads,
            /* dependencies */
               (SELECT json_agg(json_build_object('name', pvd.name,
                                                  'project_id', pvd.project_id,
                                                  'required', pvd.required,
                                                  'external_url', pvd.external_url,
                                                  'platform', pvd.platform)) AS value
                FROM project_version_dependencies pvd
                WHERE pvd.version_id = pv.id)                         AS plugin_dependencies,
               pv.platforms                                           AS platform_dependencies,
               'dum'                                                  AS platform_dependencies_formatted,
            /* members */
               (SELECT array_agg(lower(u.name))
                FROM project_members_all pma
                    JOIN users u ON pma.user_id = u.id
                WHERE pma.id = pv.project_id)                         AS member_names
     FROM project_versions pv
         JOIN project_channels pc ON pv.channel_id = pc.id
         LEFT JOIN version_stats_view vsv ON pv.id = vsv.id
    <where>
    """)
    List<Version> getAllVersions(@Define String where);
}
