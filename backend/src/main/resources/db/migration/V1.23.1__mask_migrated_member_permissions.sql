-- Memberships migrated from the old roles carry bits the member editor cannot represent (the old Project_Admin role
-- folded in edit_api_keys, a personal setting every signed-in user already has). Clamp them to what
-- MemberPermissions actually offers, so an existing membership round-trips through the editor unchanged.

-- edit_subject_settings | manage_subject_members | is_subject_owner | is_subject_member
-- | edit_page | delete_project | create_version | edit_version | delete_version | edit_tags
UPDATE user_project_roles
SET permissions = (permissions::bigint & 63216)::bit(64);

-- the above, plus create_project | post_as_organization | delete_organization
UPDATE user_organization_roles
SET permissions = (permissions::bigint & 6354928)::bit(64);
