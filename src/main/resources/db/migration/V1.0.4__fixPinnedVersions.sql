CREATE OR REPLACE VIEW pinned_versions AS
    SELECT * FROM (
        SELECT DISTINCT ON (version_id) version_id,
                                      id,
                                      created_at,
                                      type,
                                      version_string,
                                      platforms,
                                      project_id
        FROM (
               SELECT ppv.id,
                      ppv.version_id,
                      pv.created_at,
                      pv.version_string,
                      array(SELECT DISTINCT pv.platform FROM project_version_platform_dependencies pvpd
                                                                 JOIN platform_versions pv ON pv.id = pvpd.platform_version_id
                            WHERE pvpd.platform_version_id = pv.id
                            ORDER BY pv.platform
                          ) AS platforms,
                      'version' AS type,
                      pv.project_id
               FROM pinned_project_versions ppv
                        JOIN project_versions pv ON pv.id = ppv.version_id
               UNION ALL
               SELECT pc.id,
                      pv.id AS version_id,
                      pv.created_at,
                      pv.version_string,
                      array(SELECT DISTINCT pv.platform FROM project_version_platform_dependencies pvpd
                                                                 JOIN platform_versions pv ON pv.id = pvpd.platform_version_id
                            WHERE pvpd.platform_version_id = pv.id
                            ORDER BY pv.platform
                          ) AS platforms,
                      'channel' as type,
                      pv.project_id
               FROM project_channels pc
                        JOIN project_versions pv ON pc.id = pv.channel_id
               WHERE 3 = ANY(pc.flags)
           ) AS pvs
    ) AS t ORDER BY t.created_at DESC;
