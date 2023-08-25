DROP INDEX idx_projects_owner_slug_lower;
CREATE INDEX idx_projects_owner_slug_lower ON projects (lower(slug));

ALTER TABLE projects
    DROP CONSTRAINT projects_owner_name_name_key;

ALTER TABLE projects
    ADD CONSTRAINT projects_name_unique UNIQUE (name);
ALTER TABLE projects
    ADD CONSTRAINT projects_slug_unique UNIQUE (slug);
