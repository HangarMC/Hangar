SELECT d.day                                                                                                    AS day,
       (SELECT count(*) FROM project_version_reviews WHERE ended_at::date = d.day)                              AS reviews,
       (SELECT count(*) FROM project_versions WHERE created_at::date = d.day)                                   AS uploads,
       (SELECT coalesce(sum(pvd.downloads), 0) FROM project_versions_downloads pvd WHERE pvd.day = d.day)       AS downloads,
       (SELECT coalesce(sum(pv.views), 0) FROM project_views pv WHERE pv.day = d.day)                           AS views,
       (SELECT count(*) FROM projects WHERE created_at::date = d.day)                                           AS newprojects,
       (SELECT count(*) FROM users WHERE created_at::date = d.day)                                              AS newusers,
       (SELECT count(*) FROM project_flags WHERE created_at::date = d.day)                                      AS flagsopened,
       (SELECT count(*) FROM project_flags WHERE resolved_at::date = d.day)                                     AS flagsclosed
FROM (SELECT generate_series(:startDate::timestamp, :endDate::timestamp, INTERVAL '1 DAY')::date AS day) d
ORDER BY d.day
