CREATE TABLE jar_scan_result
(
    id               bigserial                NOT NULL
        CONSTRAINT jar_scan_result_pkey
            PRIMARY KEY,
    version_id       integer                  NOT NULL
        CONSTRAINT jar_scan_result_version_id_fk
            REFERENCES project_versions,
    platform         bigint                   NOT NULL,
    data             jsonb                    NOT NULL,
    highest_severity text                     NOT NULL,
    created_at       timestamp WITH TIME ZONE NOT NULL
);
