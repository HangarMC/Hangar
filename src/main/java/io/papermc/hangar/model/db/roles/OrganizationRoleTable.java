package io.papermc.hangar.model.db.roles;

import io.papermc.hangar.model.Role;

import java.time.OffsetDateTime;

public class OrganizationRoleTable extends RoleTable {

    private final long organizationId;

    public OrganizationRoleTable(OffsetDateTime createdAt, long id, long userId, Role role, boolean isAccepted, long organizationId) {
        super(createdAt, id, userId, role, isAccepted);
        this.organizationId = organizationId;
    }

    public OrganizationRoleTable(long userId, Role role, boolean isAccepted, long organizationId) {
        super(userId, role, isAccepted);
        this.organizationId = organizationId;
    }

    public long getOrganizationId() {
        return organizationId;
    }
}
