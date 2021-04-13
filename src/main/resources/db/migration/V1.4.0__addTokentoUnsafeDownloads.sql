ALTER TABLE project_version_unsafe_downloads DROP COLUMN download_type;

ALTER TABLE project_version_download_warnings RENAME expiration TO expiresAt;
