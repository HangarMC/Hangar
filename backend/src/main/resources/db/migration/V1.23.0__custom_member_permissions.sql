-- Project and organization memberships carry their own permission set and title instead of pointing at a fixed role.

ALTER TABLE user_project_roles
    ADD COLUMN permissions bit(64) DEFAULT '0'::bit(64) NOT NULL,
    ADD COLUMN title       varchar(32),
    ADD COLUMN is_owner    boolean DEFAULT FALSE        NOT NULL;

ALTER TABLE user_organization_roles
    ADD COLUMN permissions bit(64) DEFAULT '0'::bit(64) NOT NULL,
    ADD COLUMN title       varchar(32),
    ADD COLUMN is_owner    boolean DEFAULT FALSE        NOT NULL;

UPDATE user_project_roles upr
SET permissions = r.permission,
    title       = left(r.title, 32),
    is_owner    = r.name = 'Project_Owner'
FROM roles r
WHERE r.name = upr.role_type;

UPDATE user_organization_roles uor
SET permissions = r.permission,
    title       = left(r.title, 32),
    is_owner    = r.name = 'Organization_Owner'
FROM roles r
WHERE r.name = uor.role_type;

-- rows whose role_type no longer resolves would otherwise block the NOT NULL below
UPDATE user_project_roles SET title = 'Member' WHERE title IS NULL;
UPDATE user_organization_roles SET title = 'Member' WHERE title IS NULL;

ALTER TABLE user_project_roles ALTER COLUMN title SET NOT NULL;
ALTER TABLE user_organization_roles ALTER COLUMN title SET NOT NULL;

-- both read role_type; R__01_trust_views.sql recreates them from the new columns once this migration lands
DROP VIEW IF EXISTS project_trust;
DROP VIEW IF EXISTS organization_trust;

ALTER TABLE user_project_roles
    DROP CONSTRAINT user_project_roles_role_type_fkey,
    DROP COLUMN role_type;

ALTER TABLE user_organization_roles
    DROP CONSTRAINT user_organization_roles_role_type_fkey,
    DROP COLUMN role_type;

-- the roles table is global-only from here on
DELETE FROM roles WHERE category <> 'global';
