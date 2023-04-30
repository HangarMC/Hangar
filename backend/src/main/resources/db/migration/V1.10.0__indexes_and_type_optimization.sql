-- Be more specific about the length of a few columns
ALTER TABLE api_keys ALTER COLUMN token_identifier TYPE char(36), ALTER COLUMN token TYPE char(64);
-- ALTER TABLE project_versions ALTER COLUMN version_string TYPE varchar(32);
ALTER TABLE jar_scan_result ALTER COLUMN highest_severity TYPE varchar(32);
ALTER TABLE jar_scan_result_entry ALTER COLUMN severity TYPE varchar(32);
ALTER TABLE project_version_dependencies ALTER COLUMN external_url TYPE varchar(255), ALTER COLUMN name TYPE varchar(64);
--ALTER TABLE projects ALTER COLUMN tags TYPE varchar(64)[];

-- Create a whole lot of indexes
CREATE INDEX idx_project_versions_downloads_individual_cookie_user ON project_versions_downloads_individual (user_id, cookie);
--CREATE INDEX idx_project_versions_downloads_project_version ON project_versions_downloads (project_id, version_id); Already exists
CREATE INDEX idx_avatars_subject_type ON avatars (subject, type);
CREATE INDEX idx_jar_scan_result_version_platform ON jar_scan_result (version_id, platform);
CREATE INDEX idx_jar_scan_result_entry_result ON jar_scan_result_entry (result_id);
CREATE INDEX idx_jobs_state ON jobs (state);
CREATE INDEX idx_notifications_user ON notifications (user_id);
CREATE INDEX idx_notifications_user_read ON notifications (user_id, read);
CREATE INDEX idx_organization_members_id ON organization_members (organization_id);
CREATE INDEX idx_organizations_id ON organizations (id);
CREATE INDEX idx_pinned_user_projects_project ON pinned_user_projects (project_id);
CREATE INDEX idx_project_channels_project ON project_channels (project_id);
CREATE INDEX idx_project_flag_notifications_flag ON project_flag_notifications (flag_id);
CREATE INDEX idx_project_home_pages_project_page ON project_home_pages (project_id, page_id);
CREATE INDEX idx_project_members_project_user ON project_members (project_id, user_id);
CREATE INDEX idx_project_notes_project ON project_notes (project_id);
CREATE INDEX idx_project_pages_id ON project_pages (project_id);
CREATE INDEX idx_project_stars_project ON project_stars (project_id);
CREATE INDEX idx_project_version_dependencies_project ON project_version_dependencies (project_id);
CREATE INDEX idx_project_version_downloads_version ON project_version_downloads (version_id);
CREATE INDEX idx_project_version_platform_dependencies_version_platform ON project_version_platform_dependencies (version_id, platform_version_id);
CREATE INDEX idx_project_version_review_messages_review ON project_version_review_messages (review_id);
CREATE INDEX idx_project_versions_id ON project_versions (id);
--CREATE INDEX idx_project_versions_version ON project_versions (version_string); Already exists
CREATE INDEX idx_project_views_individual_cookie_user ON project_views_individual (user_id, cookie);
CREATE INDEX idx_project_visibility_changes_project ON project_visibility_changes (project_id);
CREATE INDEX idx_projects_id ON projects (id);
CREATE INDEX idx_projects_owner_slug ON projects (slug, owner_name);
CREATE INDEX idx_roles_id ON roles (id);
CREATE INDEX idx_user_credentials_type_user ON user_credentials (type, user_id);
CREATE INDEX idx_user_global_roles_user ON user_global_roles (user_id);
CREATE INDEX idx_user_organization_roles_user ON user_organization_roles (user_id);
CREATE INDEX idx_user_project_roles_user ON user_project_roles (user_id);
CREATE INDEX idx_users_id ON users (id);
CREATE INDEX idx_users_name ON users (name);
CREATE INDEX idx_users_history_uuid ON users_history (uuid);
CREATE INDEX idx_verification_codes_type_user ON verification_codes (type, user_id);
