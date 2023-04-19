SELECT (SELECT count(*) FROM project_version_reviews WHERE ended_at::date = day)                                               reviews,
       (SELECT count(*) FROM project_versions WHERE created_at::date = day)                                                    uploads,
       (SELECT count(*) FROM project_versions_downloads_individual WHERE created_at = day)                                     totaldownloads,
       (SELECT count(*) FROM project_flags WHERE created_at::date <= day AND (created_at::date >= day OR resolved_at IS NULL)) flagsopened,
       (SELECT count(*) FROM project_flags WHERE resolved_at::date = day)                                                      flagsclosed,
       day::date
FROM (SELECT generate_series(:startDate::timestamp, :endDate::timestamp, INTERVAL '1 DAY') as day) dates
ORDER BY day
