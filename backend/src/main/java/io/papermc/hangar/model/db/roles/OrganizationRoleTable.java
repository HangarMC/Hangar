package io.papermc.hangar.model.db.roles;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.jdbi.v3.core.annotation.Unmappable;
import org.jdbi.v3.core.mapper.reflect.ColumnName;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;
import org.springframework.lang.Nullable;

import java.time.OffsetDateTime;

import io.papermc.hangar.model.common.roles.OrganizationRole;
import io.papermc.hangar.model.internal.logs.contexts.OrganizationContext;
import io.papermc.hangar.model.loggable.OrganizationLoggable;

public class OrganizationRoleTable extends ExtendedRoleTable<OrganizationRole, OrganizationContext> implements OrganizationLoggable {

    private final long organizationId;
    private final long ownerId;
    private final String ownerName;

    @JdbiConstructor
    public OrganizationRoleTable(OffsetDateTime createdAt, long id, long userId, @ColumnName("role_type") OrganizationRole role, boolean accepted, long organizationId, @Nullable Long ownerId, @Nullable String ownerName) {
        super(createdAt, id, userId, role, accepted);
        this.organizationId = organizationId;
        this.ownerId = ownerId == null ? -1 : ownerId;
        this.ownerName = ownerName;
    }

    public OrganizationRoleTable(long userId, OrganizationRole role, boolean accepted, long organizationId) {
        super(userId, role, accepted);
        this.organizationId = organizationId;
        this.ownerId = -1;
        this.ownerName = null;
    }

    @JsonIgnore
    public long getOrganizationId() {
        return organizationId;
    }

    @Override
    @Unmappable
    public long getPrincipalId() {
        return organizationId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public long getOwnerId() {
        return ownerId;
    }

    @Override
    public String toString() {
        return "OrganizationRoleTable{" +
                "organizationId=" + organizationId +
                "} " + super.toString();
    }
}
