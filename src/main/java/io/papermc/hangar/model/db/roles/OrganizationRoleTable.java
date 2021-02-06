package io.papermc.hangar.model.db.roles;


import io.papermc.hangar.model.common.roles.OrganizationRole;
import org.jdbi.v3.core.mapper.reflect.ColumnName;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

import java.time.OffsetDateTime;

public class OrganizationRoleTable extends ExtendedRoleTable<OrganizationRole> {

    private final long organizationId;

    @JdbiConstructor
    public OrganizationRoleTable(OffsetDateTime createdAt, long id, long userId, @ColumnName("role_type") OrganizationRole role, boolean accepted, long organizationId) {
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
