DROP MATERIALIZED VIEW home_projects;
DROP TABLE recommended_project_versions;
DROP TABLE project_version_tags;

CREATE MATERIALIZED VIEW home_projects AS
SELECT p.id,
       p.owner_name,
       array_agg(DISTINCT pm.user_id)            AS project_members,
       p.slug,
       p.visibility,
       COALESCE(pva.views::bigint, 0::bigint)            AS views,
       COALESCE(pda.downloads::bigint, 0::bigint)        AS downloads,
       COALESCE(pvr.recent_views::bigint, 0::bigint)     AS recent_views,
       COALESCE(pdr.recent_downloads::bigint, 0::bigint) AS recent_downloads,
       COALESCE(ps.stars::bigint, 0::bigint)             AS stars,
       COALESCE(pw.watchers::bigint, 0::bigint)          AS watchers,
       p.category,
       p.description,
       p.name,
       p.created_at,
       p.license_type,
       max(lv.created_at)                        AS last_updated,
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
               'D'::"char")                  AS search_words
FROM projects p
         LEFT JOIN project_versions lv ON p.id = lv.project_id
         JOIN project_members_all pm ON p.id = pm.id
         LEFT JOIN (SELECT p_1.id,
                           COUNT(ps_1.user_id) AS stars
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
                    WHERE pv.day >= (CURRENT_DATE - '30 days'::interval)
                      AND pv.day <= CURRENT_DATE
                    GROUP BY pv.project_id) pvr ON p.id = pvr.project_id
         LEFT JOIN (SELECT pv.project_id,
                           sum(pv.downloads) AS recent_downloads
                    FROM project_versions_downloads pv
                    WHERE pv.day >= (CURRENT_DATE - '30 days'::interval)
                      AND pv.day <= CURRENT_DATE
                    GROUP BY pv.project_id) pdr ON p.id = pdr.project_id
GROUP BY p.id, ps.stars, pw.watchers, pva.views, pda.downloads, pvr.recent_views, pdr.recent_downloads;
