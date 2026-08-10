CREATE TABLE discovery_exclusions
(
    project_id bigint                   NOT NULL
        CONSTRAINT discovery_exclusions_project_id_fkey
            REFERENCES projects
            ON DELETE CASCADE,
    created_at timestamp WITH TIME ZONE NOT NULL DEFAULT now(),
    -- kept nullable so removing a moderator's account doesn't put a project they pulled back into rotation
    created_by bigint
        CONSTRAINT discovery_exclusions_created_by_fkey
            REFERENCES users
            ON DELETE SET NULL,
    CONSTRAINT discovery_exclusions_pkey
        PRIMARY KEY (project_id)
);
