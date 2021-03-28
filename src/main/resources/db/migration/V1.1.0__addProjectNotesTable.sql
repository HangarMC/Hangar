ALTER TABLE project_visibility_changes DROP CONSTRAINT project_visibility_changes_created_by_fkey;
ALTER TABLE project_visibility_changes ADD CONSTRAINT project_visibility_changes_created_by_fkey FOREIGN KEY (created_by) REFERENCES users ON DELETE SET NULL;

ALTER TABLE project_visibility_changes DROP CONSTRAINT project_visibility_changes_resolved_by_fkey;
ALTER TABLE project_visibility_changes ADD CONSTRAINT project_visibility_changes_resolved_by_fkey FOREIGN KEY (resolved_by) REFERENCES users ON DELETE SET NULL;

ALTER TABLE project_version_visibility_changes DROP CONSTRAINT project_version_visibility_changes_created_by_fkey;
ALTER TABLE project_version_visibility_changes ADD CONSTRAINT project_version_visibility_changes_created_by_fkey FOREIGN KEY (created_by) REFERENCES users ON DELETE SET NULL;

ALTER TABLE project_version_visibility_changes DROP CONSTRAINT project_version_visibility_changes_resolved_by_fkey;
ALTER TABLE project_version_visibility_changes ADD CONSTRAINT project_version_visibility_changes_resolved_by_fkey FOREIGN KEY (resolved_by) REFERENCES users ON DELETE SET NULL;

CREATE TABLE project_notes
(
    id bigserial NOT NULL
        CONSTRAINT notes_pkey PRIMARY KEY,
    created_at timestamp with time zone NOT NULL,
    project_id bigint NOT NULL
        CONSTRAINT notes_project_id_fkey
            REFERENCES projects ON DELETE CASCADE,
    message text NOT NULL,
    user_id bigint
        CONSTRAINT notes_user_id
            REFERENCES users ON DELETE SET NULL
);

ALTER TABLE projects DROP COLUMN notes;
