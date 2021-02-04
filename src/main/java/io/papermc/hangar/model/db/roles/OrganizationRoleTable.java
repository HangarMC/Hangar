package io.papermc.hangar.model.db.roles;


import io.papermc.hangar.model.roles.OrganizationRole;

import java.time.OffsetDateTime;

public class OrganizationRoleTable extends ExtendedRoleTable<OrganizationRole> {

    private final long organizationId;

    public OrganizationRoleTable(OffsetDateTime createdAt, long id, long userId, OrganizationRole role, boolean accepted, long organizationId) {
        super(createdAt, id, userId, role, accepted);
        this.organizationId = organizationId;
    }

    public OrganizationRoleTable(long userId, OrganizationRole role, boolean accepted, long organizationId) {
        super(userId, role, accepted);
        this.organizationId = organizationId;
    }

    public long getOrganizationId() {
        return organizationId;
    }

    @Override
    public long getPrincipalId() {
        return organizationId;
    }

    @Override
    public String toString() {
        return "OrganizationRoleTable{" +
                "organizationId=" + organizationId +
                "} " + super.toString();
    }
}
