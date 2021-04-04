SELECT
       (SELECT count(*) FROM project_version_reviews WHERE ended_at::date = day) reviews,
       (SELECT count(*) FROM project_versions WHERE created_at::date = day) uploads,
       (SELECT count(*) FROM project_versions_downloads_individual WHERE created_at = day) totalDownloads,
       (SELECT count(*) FROM project_version_unsafe_downloads WHERE created_at::date = day) unsafeDownloads,
       (SELECT count(*) FROM project_flags WHERE created_at::date <= day AND (created_at::date >= day OR resolved_at IS NULL)) flagsOpened,
       (SELECT count(*) FROM project_flags WHERE resolved_at::date = day) flagsClosed,
       day::date
FROM (SELECT generate_series(:startDate, :endDate, INTERVAL '1 DAY') AS day) dates
ORDER BY day