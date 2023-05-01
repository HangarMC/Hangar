CREATE INDEX idx_users_name_lower ON users (lower(name));
CREATE INDEX idx_projects_owner_slug_lower ON projects (lower(slug), lower(owner_name));
DROP INDEX idx_users_name;
DROP INDEX idx_projects_owner_slug;
