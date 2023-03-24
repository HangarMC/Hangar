ALTER TABLE projects
    ADD COLUMN tags text[] DEFAULT ARRAY []::text[] NOT NULL;
