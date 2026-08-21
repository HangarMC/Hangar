DROP VIEW IF EXISTS pinned_versions CASCADE;

CREATE OR REPLACE VIEW pinned_versions AS
    SELECT *
    FROM (SELECT DISTINCT ON (project_id, version_id) version_id,
                                          id,
                                          created_at,
                                          type,
                                          version_string,
                                          platforms,
                                          project_id
          FROM (SELECT ppv.id,
                       ppv.version_id,
                       pv.created_at,
                       pv.version_string,
                       pv.platforms AS platforms,
                       'version'    AS type,
                       pv.project_id
                FROM pinned_project_versions ppv
                    JOIN project_versions pv ON pv.id = ppv.version_id
                UNION ALL
                (SELECT pc.id,
                        pv.id        AS version_id,
                        pv.created_at,
                        pv.version_string,
                        pv.platforms AS platforms,
                        'channel'    AS type,
                        pv.project_id
                 FROM project_channels pc
                     JOIN project_versions pv ON pc.id = pv.channel_id
                 WHERE 2 = ANY (pc.flags)
                 ORDER BY pv.created_at DESC)) AS pvs
          -- a version can be both explicitly pinned and the head of a pinned channel; prefer explicit pin
          ORDER BY project_id, version_id, type DESC) AS t
    ORDER BY t.created_at DESC;
