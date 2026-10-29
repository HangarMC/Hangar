ALTER TABLE api_keys
    ADD COLUMN expires_at     timestamp WITH TIME ZONE,
    -- kept separate from the join table below: deleting the last scoped project must not silently widen the key
    ADD COLUMN project_scoped boolean NOT NULL DEFAULT FALSE;

CREATE TABLE api_key_projects
(
    key_id     bigint NOT NULL
        CONSTRAINT api_key_projects_key_id_fkey
            REFERENCES api_keys
            ON DELETE CASCADE,
    project_id bigint NOT NULL
        CONSTRAINT api_key_projects_project_id_fkey
            REFERENCES projects
            ON DELETE CASCADE,
    CONSTRAINT api_key_projects_pkey
        PRIMARY KEY (key_id, project_id)
);

CREATE INDEX idx_api_key_projects_project ON api_key_projects (project_id);
