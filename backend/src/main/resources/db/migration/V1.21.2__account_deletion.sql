ALTER TABLE users
    ADD COLUMN deletion_requested_at timestamp WITH TIME ZONE;

CREATE INDEX users_deletion_requested_at_idx ON users (deletion_requested_at)
    WHERE deletion_requested_at IS NOT NULL;

ALTER TABLE users_history
    DROP CONSTRAINT users_history_users_uuid_fk,
    ADD CONSTRAINT users_history_users_uuid_fk FOREIGN KEY (uuid) REFERENCES users (uuid) ON DELETE CASCADE;

ALTER TABLE project_visibility_changes
    ALTER COLUMN created_by DROP NOT NULL,
    DROP CONSTRAINT project_visibility_changes_created_by_fkey,
    ADD CONSTRAINT project_visibility_changes_created_by_fkey FOREIGN KEY (created_by) REFERENCES users ON DELETE SET NULL;

ALTER TABLE project_version_visibility_changes
    ALTER COLUMN created_by DROP NOT NULL,
    DROP CONSTRAINT project_version_visibility_changes_created_by_fkey,
    ADD CONSTRAINT project_version_visibility_changes_created_by_fkey FOREIGN KEY (created_by) REFERENCES users ON DELETE SET NULL;

ALTER TABLE announcements
    ALTER COLUMN created_by DROP NOT NULL,
    DROP CONSTRAINT announcements_created_by_fkey,
    ADD CONSTRAINT announcements_created_by_fkey FOREIGN KEY (created_by) REFERENCES users ON DELETE SET NULL;

ALTER TABLE global_notifications
    ALTER COLUMN created_by DROP NOT NULL,
    DROP CONSTRAINT global_notifications_created_by_fkey,
    ADD CONSTRAINT global_notifications_created_by_fkey FOREIGN KEY (created_by) REFERENCES users ON DELETE SET NULL;
