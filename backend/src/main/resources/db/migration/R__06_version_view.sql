DROP VIEW IF EXISTS version_view;

CREATE OR REPLACE VIEW version_view AS
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
