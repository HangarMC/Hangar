ALTER TABLE project_versions_downloads
    ADD COLUMN platform bigint NOT NULL DEFAULT -1,
    DROP CONSTRAINT project_versions_downloads_pkey;

ALTER TABLE project_versions_downloads
    ADD PRIMARY KEY (day, version_id, platform);

ALTER TABLE project_versions_downloads_individual
    ADD COLUMN platform bigint NOT NULL DEFAULT -1;
