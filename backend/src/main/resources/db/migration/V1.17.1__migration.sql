UPDATE project_version_downloads pvd
SET download_platform = (SELECT platform FROM project_version_platform_downloads WHERE download_id = pvd.id LIMIT 1),
    platforms         = (SELECT array_agg(platform) from project_version_platform_downloads WHERE download_id = pvd.id)
WHERE platforms IS NULL;
