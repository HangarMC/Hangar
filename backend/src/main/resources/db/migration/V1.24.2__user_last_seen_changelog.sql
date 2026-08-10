-- Null means "never opened the changelog"; the frontend then treats every entry as unseen.
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS last_seen_changelog_at TIMESTAMPTZ;
