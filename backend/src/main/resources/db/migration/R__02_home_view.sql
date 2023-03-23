CREATE OR REPLACE VIEW projects_extra AS
    SELECT p.*,
           array_agg(DISTINCT pm.user_id)           AS project_members,
           max(lv.created_at)                       AS last_updated,
           coalesce(ps.stars::bigint, 0::bigint)    AS stars,
           coalesce(pw.watchers::bigint, 0::bigint) AS watchers
    FROM projects p
        JOIN project_members_all pm ON p.id = pm.id
        LEFT JOIN project_versions lv ON p.id = lv.project_id
        LEFT JOIN (SELECT p_1.id,
                          count(ps_1.user_id) AS stars
                   FROM projects p_1
                       LEFT JOIN project_stars ps_1 ON p_1.id = ps_1.project_id
                   GROUP BY p_1.id) ps ON p.id = ps.id
        LEFT JOIN (SELECT p_1.id,
                          count(pw_1.user_id) AS watchers
                   FROM projects p_1
                       LEFT JOIN project_watchers pw_1 ON p_1.id = pw_1.project_id
                   GROUP BY p_1.id) pw ON p.id = pw.id
    GROUP BY p.id, ps.stars, pw.watchers;


DROP MATERIALIZED VIEW IF EXISTS home_projects CASCADE;

CREATE MATERIALIZED VIEW home_projects AS
    SELECT p.id,
           array_agg(DISTINCT pm.user_id)                    AS project_members,
           coalesce(pva.views::bigint, 0::bigint)            AS views,
           coalesce(pda.downloads::bigint, 0::bigint)        AS downloads,
           coalesce(pvr.recent_views::bigint, 0::bigint)     AS recent_views,
           coalesce(pdr.recent_downloads::bigint, 0::bigint) AS recent_downloads,
           coalesce(ps.stars::bigint, 0::bigint)             AS stars,
           coalesce(pw.watchers::bigint, 0::bigint)          AS watchers,
           max(lv.created_at)                                AS last_updated,
           ((setweight((to_tsvector('english'::regconfig, p.name::text) ||
                        to_tsvector('english'::regconfig, regexp_replace(p.name::text, '([a-z])([A-Z]+)'::text,
                                                                         '\1_\2'::text, 'g'::text))), 'A'::"char") ||
             setweight(to_tsvector('english'::regconfig, p.description::text), 'B'::"char")) ||
            setweight(to_tsvector('english'::regconfig, array_to_string(p.keywords, ' '::text)), 'C'::"char")) || setweight(
                   to_tsvector('english'::regconfig, p.owner_name::text) || to_tsvector('english'::regconfig,
                                                                                        regexp_replace(
                                                                                            p.owner_name::text,
                                                                                            '([a-z])([A-Z]+)'::text,
                                                                                            '\1_\2'::text,
                                                                                            'g'::text)),
                   'D'::"char")                              AS search_words
    FROM projects p
        LEFT JOIN project_versions lv ON p.id = lv.project_id
        JOIN project_members_all pm ON p.id = pm.id
        LEFT JOIN (SELECT p_1.id,
                          count(ps_1.user_id) AS stars
                   FROM projects p_1
                       LEFT JOIN project_stars ps_1 ON p_1.id = ps_1.project_id
                   GROUP BY p_1.id) ps ON p.id = ps.id
        LEFT JOIN (SELECT p_1.id,
                          count(pw_1.user_id) AS watchers
                   FROM projects p_1
                       LEFT JOIN project_watchers pw_1 ON p_1.id = pw_1.project_id
                   GROUP BY p_1.id) pw ON p.id = pw.id
        LEFT JOIN (SELECT pv.project_id,
                          sum(pv.views) AS views
                   FROM project_views pv
                   GROUP BY pv.project_id) pva ON p.id = pva.project_id
        LEFT JOIN (SELECT pv.project_id,
                          sum(pv.downloads) AS downloads
                   FROM project_versions_downloads pv
                   GROUP BY pv.project_id) pda ON p.id = pda.project_id
        LEFT JOIN (SELECT pv.project_id,
                          sum(pv.views) AS recent_views
                   FROM project_views pv
                   WHERE pv.day >= (current_date - '30 days'::interval)
                     AND pv.day <= current_date
                   GROUP BY pv.project_id) pvr ON p.id = pvr.project_id
        LEFT JOIN (SELECT pv.project_id,
                          sum(pv.downloads) AS recent_downloads
                   FROM project_versions_downloads pv
                   WHERE pv.day >= (current_date - '30 days'::interval)
                     AND pv.day <= current_date
                   GROUP BY pv.project_id) pdr ON p.id = pdr.project_id
    GROUP BY p.id, ps.stars, pda.downloads, pdr.recent_downloads, pw.watchers, pva.views, pvr.recent_views;
