package io.papermc.hangar.model.db.roles;


import io.papermc.hangar.model.roles.OrganizationRole;

import java.time.OffsetDateTime;

public class OrganizationRoleTable extends ExtendedRoleTable<OrganizationRole> {

    private final long organizationId;

    public OrganizationRoleTable(OffsetDateTime createdAt, long id, long userId, OrganizationRole role, boolean isAccepted, long organizationId) {
        super(createdAt, id, userId, role, isAccepted);
        this.organizationId = organizationId;
    }

    public OrganizationRoleTable(long userId, OrganizationRole role, boolean isAccepted, long organizationId) {
        super(userId, role, isAccepted);
        this.organizationId = organizationId;
    }

    public long getOrganizationId() {
        return organizationId;
    }

    @Override
    public String toString() {
        return "OrganizationRoleTable{" +
                "organizationId=" + organizationId +
                "} " + super.toString();
    }
}
