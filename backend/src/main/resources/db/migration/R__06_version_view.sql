DROP MATERIALIZED VIEW IF EXISTS version_stats_view CASCADE;

CREATE MATERIALIZED VIEW version_stats_view AS
    SELECT pv.id,
           coalesce((SELECT sum(pvd.downloads) FROM project_versions_downloads pvd WHERE pv.id = pvd.version_id),
                    0)                      AS totaldownloads,
           (SELECT json_agg(d.res)
            FROM (SELECT json_build_object('platform', pvd.platform, 'downloads', sum(pvd.downloads)) res
                  FROM project_versions_downloads pvd
                  WHERE pv.id = pvd.version_id
                  GROUP BY pvd.platform) d) AS platformdownloads
    FROM project_versions pv;

CREATE UNIQUE INDEX version_stats_view_id_idx ON version_stats_view (id);

DROP MATERIALIZED VIEW IF EXISTS project_stats_view CASCADE;

CREATE MATERIALIZED VIEW project_stats_view AS
    SELECT p.id,
           p.name,
           coalesce(pva.views::bigint, 0::bigint)            AS views,
           coalesce(pda.downloads::bigint, 0::bigint)        AS downloads,
           coalesce(pvr.recent_views::bigint, 0::bigint)     AS recent_views,
           coalesce(pdr.recent_downloads::bigint, 0::bigint) AS recent_downloads,
           coalesce(ps.stars::bigint, 0::bigint)             AS stars,
           coalesce(pw.watchers::bigint, 0::bigint)          AS watchers,
           coalesce((SELECT pv.created_at
                     FROM project_versions pv
                     WHERE pv.project_id = p.id
                     ORDER BY pv.created_at DESC
                     LIMIT 1), p.created_at)                 AS last_updated
    FROM projects p
        LEFT JOIN project_versions lv ON p.id = lv.project_id
        /* stars */
        LEFT JOIN (SELECT p_1.id,
                          count(ps_1.user_id) AS stars
                   FROM projects p_1
                       LEFT JOIN project_stars ps_1 ON p_1.id = ps_1.project_id
                   GROUP BY p_1.id) ps ON p.id = ps.id
        /* watchers */
        LEFT JOIN (SELECT p_1.id,
                          count(pw_1.user_id) AS watchers
                   FROM projects p_1
                       LEFT JOIN project_watchers pw_1 ON p_1.id = pw_1.project_id
                   GROUP BY p_1.id) pw ON p.id = pw.id
        /* views */
        LEFT JOIN (SELECT pv.project_id,
                          sum(pv.views) AS views
                   FROM project_views pv
                   GROUP BY pv.project_id) pva ON p.id = pva.project_id
        /* downloads */
        LEFT JOIN (SELECT pv.project_id,
                          sum(pv.downloads) AS downloads
                   FROM project_versions_downloads pv
                   GROUP BY pv.project_id) pda ON p.id = pda.project_id
        /* recent views */
        LEFT JOIN (SELECT pv.project_id,
                          sum(pv.views) AS recent_views
                   FROM project_views pv
                   WHERE pv.day >= (current_date - '30 days'::interval)
                     AND pv.day <= current_date
                   GROUP BY pv.project_id) pvr ON p.id = pvr.project_id
        /* recent downloads */
        LEFT JOIN (SELECT pv.project_id,
                          sum(pv.downloads) AS recent_downloads
                   FROM project_versions_downloads pv
                   WHERE pv.day >= (current_date - '30 days'::interval)
                     AND pv.day <= current_date
                   GROUP BY pv.project_id) pdr ON p.id = pdr.project_id
    GROUP BY p.id, ps.stars, pw.watchers, pva.views, pda.downloads, pvr.recent_views, pdr.recent_downloads;

CREATE UNIQUE INDEX project_stats_view_id_idx ON project_stats_view (id);

DROP MATERIALIZED VIEW IF EXISTS version_view;

CREATE MATERIALIZED VIEW version_view AS
    SELECT pv.id,
           pv.created_at,
           pv.version_string,
           pv.visibility,
           pv.description,
           pv.project_id,
           vsv.totaldownloads                                     AS vs_totaldownloads,
           vsv.platformdownloads                                  AS vs_platformdownloads,
           (SELECT u.name FROM users u WHERE u.id = pv.author_id) AS author,
           pv.review_state,
           pc.created_at                                          AS pc_created_at,
           pc.name                                                AS pc_name,
           pc.description                                         AS pc_description,
           pc.color                                               AS pc_color,
           pc.flags                                               AS pc_flags,
           CASE
               WHEN exists(SELECT * FROM pinned_versions piv WHERE piv.version_id = pv.id AND lower(type) = 'channel')
                   THEN 'CHANNEL'
               WHEN exists(SELECT * FROM pinned_versions piv WHERE piv.version_id = pv.id AND lower(type) = 'version')
                   THEN 'VERSION'
               ELSE 'NONE'
               END                                                AS pinnedstatus,
           (SELECT ARRAY [p.owner_name, p.slug]
            FROM projects p
            WHERE p.id = pv.project_id
            LIMIT 1)                                              AS project_namespace,-- needed for downloads
           (SELECT json_agg(json_build_object('file_size', file_size,
                                              'hash', hash,
                                              'file_name', file_name,
                                              'external_url', external_url,
                                              'platforms', platforms,
                                              'download_platform', download_platform)) AS value
            FROM project_version_downloads
            WHERE version_id = pv.id
            GROUP BY version_id)                                  AS downloads,
           (SELECT json_agg(json_build_object('name', pvd.name,
                                              'project_id', pvd.project_id,
                                              'required', pvd.required,
                                              'external_url', pvd.external_url,
                                              'platform', pvd.platform)) AS value
            FROM project_version_dependencies pvd
            WHERE pvd.version_id = pv.id)                         AS plugin_dependencies,
           (SELECT json_agg(json_build_object(
               'platform', plv.platform,
               'version', plv.version)) AS value
            FROM project_version_platform_dependencies pvpd
                JOIN platform_versions plv ON pvpd.platform_version_id = plv.id
            WHERE pvpd.version_id = pv.id)                        AS platform_dependencies,
           'dum'                                                  AS platform_dependencies_formatted
    FROM project_versions pv
        JOIN project_channels pc ON pv.channel_id = pc.id
        JOIN version_stats_view vsv ON pv.id = vsv.id;

CREATE UNIQUE INDEX version_view_id_idx ON version_view (id);
