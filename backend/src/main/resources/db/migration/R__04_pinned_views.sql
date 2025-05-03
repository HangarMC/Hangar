DROP VIEW IF EXISTS pinned_versions CASCADE;

DROP VIEW IF EXISTS pinned_projects CASCADE;

CREATE OR REPLACE VIEW pinned_versions AS
    SELECT *
    FROM (SELECT DISTINCT ON (version_id) version_id,
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
                 ORDER BY pv.created_at DESC)) AS pvs) AS t
    ORDER BY t.created_at DESC;

CREATE OR REPLACE VIEW pinned_projects AS
    SELECT DISTINCT ON (project_id) project_id,
                                    user_id,
                                    id,
                                    owner_name AS owner,
                                    project_members,
                                    project_member_names,
                                    slug,
                                    visibility,
                                    views,
                                    downloads,
                                    recent_views,
                                    recent_downloads,
                                    stars,
                                    watchers,
                                    category,
                                    name,
                                    created_at,
                                    license_type,
                                    description,
                                    last_updated,
                                    avatar,
                                    avatar_fallback
    FROM (SELECT pp.id,
                 pp.user_id,
                 pp.project_id,
                 p.owner_name,
                 hp.project_members,
                 hp.project_member_names,
                 p.slug,
                 p.visibility,
                 hp.views,
                 hp.downloads,
                 hp.recent_views,
                 hp.recent_downloads,
                 hp.stars,
                 hp.watchers,
                 p.category,
                 p.name,
                 p.created_at,
                 p.license_type,
                 p.description,
                 hp.last_updated,
                 hp.avatar,
                 hp.avatar_fallback
          FROM pinned_user_projects pp
              JOIN home_projects hp ON hp.id = pp.project_id
              JOIN projects p ON pp.project_id = p.id) AS pvs;
