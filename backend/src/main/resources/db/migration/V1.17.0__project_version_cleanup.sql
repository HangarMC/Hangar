ALTER TABLE project_version_downloads
    ADD platforms         bigint[],
    ADD download_platform bigint
