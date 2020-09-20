ALTER TABLE project_versions ALTER COLUMN file_size DROP NOT NULL;
ALTER TABLE project_versions ALTER COLUMN hash DROP NOT NULL;
ALTER TABLE project_versions ALTER COLUMN file_name DROP NOT NULL;