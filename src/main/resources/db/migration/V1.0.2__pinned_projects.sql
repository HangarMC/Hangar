CREATE TABLE pinned_projects
(
    id         bigserial                NOT NULL
        CONSTRAINT project_pinned_pkey
            PRIMARY KEY,
    user_id    bigint                   NOT NULL
        CONSTRAINT project_pinned_user_id_fkey
            REFERENCES users
            ON DELETE CASCADE,
    project_id bigint                   NOT NULL
        CONSTRAINT project_pinned_project_id_fkey
            REFERENCES projects
            ON DELETE CASCADE,
    CONSTRAINT pinned_projects_project_user_key
        UNIQUE (project_id, user_id)
);

CREATE OR REPLACE VIEW pinned_projects AS
SELECT *
FROM (SELECT DISTINCT ON (project_id) project_id,
                                      user_id,
                                      id,
                                      owner_name as owner,
                                      project_members,
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
                                      last_updated
      FROM (SELECT pp.id,
                   pp.user_id,
                   pp.project_id,
                   hp.owner_name,
                   hp.project_members,
                   hp.slug,
                   hp.visibility,
                   hp.views,
                   hp.downloads,
                   hp.recent_views,
                   hp.recent_downloads,
                   hp.stars,
                   hp.watchers,
                   hp.category,
                   hp.name,
                   hp.created_at,
                   hp.license_type,
                   hp.last_updated
            FROM pinned_user_projects pp
                     JOIN home_projects hp ON hp.id = pp.project_id
                     JOIN projects p on pp.project_id = p.id) AS pvs) as t;
