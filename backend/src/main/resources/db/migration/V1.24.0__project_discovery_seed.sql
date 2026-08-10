-- Assigned once at creation and never rewritten: the discovery window walks this value in order, so a
-- project able to reroll its seed could park itself in front of the cursor every day. The backfill is
-- guarded on NULL for that reason, and because ADD COLUMN would evaluate the default once for every
-- existing row rather than per row the way an INSERT does.
ALTER TABLE projects ADD COLUMN IF NOT EXISTS discovery_seed double precision;
UPDATE projects SET discovery_seed = random() WHERE discovery_seed IS NULL;
ALTER TABLE projects ALTER COLUMN discovery_seed SET DEFAULT random();
ALTER TABLE projects ALTER COLUMN discovery_seed SET NOT NULL;

CREATE INDEX IF NOT EXISTS projects_discovery_seed_idx ON projects (discovery_seed);
