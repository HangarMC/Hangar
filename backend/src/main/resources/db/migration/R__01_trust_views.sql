CREATE OR REPLACE VIEW global_trust(user_id, permission) AS
    SELECT gr.user_id,
           coalesce(bit_or(r.permission), '0'::bit(64)) AS permission
    FROM user_global_roles gr
        JOIN roles r ON gr.role_id = r.id
    GROUP BY gr.user_id;

CREATE OR REPLACE VIEW project_trust(project_id, user_id, permission) AS
    SELECT pm.project_id,
           pm.user_id,
           coalesce(bit_or(r.permission), '0'::bit(64)) AS permission
    FROM project_members pm
        JOIN user_project_roles rp ON pm.project_id = rp.project_id AND pm.user_id = rp.user_id AND rp.accepted
        JOIN roles r ON rp.role_type::text = r.name::text
    GROUP BY pm.project_id, pm.user_id;

CREATE OR REPLACE VIEW organization_trust(organization_id, user_id, permission) AS
    SELECT om.organization_id,
           om.user_id,
           coalesce(bit_or(r.permission), '0'::bit(64)) AS permission
    FROM organization_members om
        JOIN user_organization_roles ro
             ON om.organization_id = ro.organization_id AND om.user_id = ro.user_id AND ro.accepted
        JOIN roles r ON ro.role_type::text = r.name::text
    GROUP BY om.organization_id, om.user_id;

CREATE OR REPLACE VIEW project_members_all(id, user_id) AS
    SELECT p.id,
           pm.user_id
    FROM projects p
        LEFT JOIN project_members pm ON p.id = pm.project_id
    UNION
    SELECT p.id,
           om.user_id
    FROM projects p
        LEFT JOIN organization_members om ON p.owner_id = om.organization_id
    WHERE om.user_id IS NOT NULL;
