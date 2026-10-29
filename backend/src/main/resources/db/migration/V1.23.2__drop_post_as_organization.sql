-- post_as_organization (bit 21) was never checked by anything and is gone; clear it so the bit is free to reuse
UPDATE user_organization_roles
SET permissions = (permissions::bigint & ~2097152)::bit(64)
WHERE (permissions::bigint & 2097152) <> 0;

UPDATE roles
SET permission = (permission::bigint & ~2097152)::bit(64)
WHERE (permission::bigint & 2097152) <> 0;
