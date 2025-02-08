alter table project_version_downloads
    alter column platforms set not null;

DROP TABLE project_version_platform_downloads;
