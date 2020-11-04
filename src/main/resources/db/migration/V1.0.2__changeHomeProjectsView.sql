DROP MATERIALIZED VIEW home_projects;

create materialized view home_projects as
WITH tags AS (
    SELECT sq.version_id,
           sq.project_id,
           sq.version_string,
           sq.tag_name,
           sq.tag_version,
           sq.tag_color
    FROM (SELECT pv.id                                                                                            AS version_id,
                 pv.project_id,
                 pv.version_string,
                 pvt.name                                                                                         AS tag_name,
                 pvt.data                                                                                         AS tag_version,
                 pvt.platform_version,
                 pvt.color                                                                                        AS tag_color,
                 row_number()
                 OVER (PARTITION BY pv.project_id, pvt.platform_version ORDER BY pv.created_at DESC)              AS row_num
          FROM project_versions pv
                   JOIN (SELECT pvti.version_id,
                                pvti.name,
                                pvti.data,
                                CASE
                                    WHEN pvti.name::text = 'Paper'::text THEN array_to_string(pvti.data, ', ')
                                    WHEN pvti.name::text = 'Waterfall'::text THEN array_to_string(pvti.data, ', ')
                                    WHEN pvti.name::text = 'Velocity'::text THEN array_to_string(pvti.data, ', ')
                                    ELSE NULL::text
                                    END AS platform_version,
                                pvti.color
                         FROM project_version_tags pvti
                         WHERE (pvti.name::text = ANY
                                (ARRAY ['Paper'::character varying, 'Waterfall'::character varying, 'Velocity'::character varying]::text[]))
                           AND pvti.data IS NOT NULL) pvt ON pv.id = pvt.version_id
          WHERE pv.visibility = 0
            AND (pvt.name::text = ANY
                 (ARRAY ['Paper'::character varying, 'Waterfall'::character varying, 'Velocity'::character varying]::text[]))
            AND pvt.platform_version IS NOT NULL) sq
    WHERE sq.row_num = 1
    ORDER BY sq.platform_version DESC
)
SELECT p.id,
       p.owner_name,
       array_agg(DISTINCT pm.user_id)            AS project_members,
       p.slug,
       p.visibility,
       COALESCE(pva.views, 0::bigint)            AS views,
       COALESCE(pda.downloads, 0::bigint)        AS downloads,
       COALESCE(pvr.recent_views, 0::bigint)     AS recent_views,
       COALESCE(pdr.recent_downloads, 0::bigint) AS recent_downloads,
       COALESCE(ps.stars, 0::bigint)             AS stars,
       COALESCE(pw.watchers, 0::bigint)          AS watchers,
       p.category,
       p.description,
       p.name,
       p.created_at,
       max(lv.created_at)                        AS last_updated,
       to_jsonb(ARRAY(SELECT jsonb_build_object('version_string', tags.version_string || '.' || tags.version_id, 'tag_name', tags.tag_name,
                                                'tag_version', tags.tag_version, 'tag_color',
                                                tags.tag_color) AS jsonb_build_object
                      FROM tags
                      WHERE tags.project_id = p.id
                      LIMIT 5))                  AS promoted_versions,
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

alter materialized view home_projects owner to hangar;