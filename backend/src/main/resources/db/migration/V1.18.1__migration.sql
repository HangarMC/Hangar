UPDATE project_versions pv
SET platforms = (WITH aggregated_versions AS (SELECT plv.platform,
                                                     json_agg(DISTINCT plv.version) AS versions
                                              FROM project_version_platform_dependencies pvpd
                                                  JOIN platform_versions plv ON pvpd.platform_version_id = plv.id
                                              WHERE pvpd.version_id = pv.id
                                              GROUP BY plv.platform)
                 SELECT json_agg(
                            json_build_object('platform', av.platform, 'versions', av.versions)
                        ) AS platform_versions
                 FROM aggregated_versions av);
