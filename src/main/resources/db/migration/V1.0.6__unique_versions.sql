TRUNCATE TABLE projects CASCADE;

ALTER TABLE project_versions ADD CONSTRAINT version_string_unique UNIQUE(project_id, version_string);

CREATE TABLE project_version_downloads
(
    id bigserial NOT NULL PRIMARY KEY,
    version_id bigint NOT NULL,
    file_size bigint default 1
        CONSTRAINT versions_file_size_check
            CHECK (file_size > 0),
    hash varchar(32),
    file_name varchar(255),
    external_url varchar(255),
    CONSTRAINT project_version_downloads_version_id_fkey
        FOREIGN KEY (version_id)
            REFERENCES project_versions
            ON DELETE CASCADE
);

CREATE TABLE project_version_platform_downloads
(
    id bigserial NOT NULL PRIMARY KEY,
    version_id bigint NOT NULL,
    platform bigint NOT NULL,
    download_id bigint NOT NULL,
    CONSTRAINT project_version_platform_downloads_version_id_fkey
        FOREIGN KEY (version_id)
            REFERENCES project_versions
            ON DELETE CASCADE,
    CONSTRAINT project_version_platform_downloads_download_id_fkey
        FOREIGN KEY (download_id)
            REFERENCES project_version_downloads
            ON DELETE CASCADE,
    CONSTRAINT project_version_platform_downloads_unique
        UNIQUE (version_id, platform)
);
