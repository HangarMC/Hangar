ALTER TABLE projects DROP COLUMN forum_sync CASCADE;
ALTER TABLE projects DROP COLUMN topic_id CASCADE;
ALTER TABLE projects DROP COLUMN post_id CASCADE;
ALTER TABLE project_versions DROP COLUMN create_forum_post CASCADE;
ALTER TABLE project_versions DROP COLUMN post_id CASCADE;
