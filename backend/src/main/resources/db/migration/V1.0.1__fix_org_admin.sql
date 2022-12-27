DELETE
FROM user_organization_roles
WHERE role_type = 'Organization_Co-Owner';

UPDATE roles
SET name='Organization_Admin'
WHERE name = 'Organization_Co-Owner';
