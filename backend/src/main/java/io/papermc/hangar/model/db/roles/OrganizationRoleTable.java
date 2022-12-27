package io.papermc.hangar.model.db.roles;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.papermc.hangar.model.common.roles.OrganizationRole;
import io.papermc.hangar.model.internal.logs.contexts.OrganizationContext;
import io.papermc.hangar.model.loggable.OrganizationLoggable;
import java.time.OffsetDateTime;
import org.jdbi.v3.core.annotation.JdbiProperty;
import org.jdbi.v3.core.mapper.reflect.ColumnName;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;
import org.springframework.lang.Nullable;

public class OrganizationRoleTable extends ExtendedRoleTable<OrganizationRole, OrganizationContext> implements OrganizationLoggable {

    private final long organizationId;
    private final long ownerId;
    private final String ownerName;

    @JdbiConstructor
    public OrganizationRoleTable(final OffsetDateTime createdAt, final long id, final long userId, @ColumnName("role_type") final OrganizationRole role, final boolean accepted, final long organizationId, @Nullable final Long ownerId, @Nullable final String ownerName) {
        super(createdAt, id, userId, role, accepted);
        this.organizationId = organizationId;
        this.ownerId = ownerId == null ? -1 : ownerId;
        this.ownerName = ownerName;
    }

    public OrganizationRoleTable(final long userId, final OrganizationRole role, final boolean accepted, final long organizationId) {
        super(userId, role, accepted);
        this.organizationId = organizationId;
        this.ownerId = -1;
        this.ownerName = null;
    }

    @JsonIgnore
    public long getOrganizationId() {
        return this.organizationId;
    }

    @Override
    @JdbiProperty(map = false)
    public long getPrincipalId() {
        return this.organizationId;
    }

    public String getOwnerName() {
        return this.ownerName;
    }

    public long getOwnerId() {
        return this.ownerId;
    }

    @Override
    public String toString() {
        return "OrganizationRoleTable{" +
            "organizationId=" + this.organizationId +
            "} " + super.toString();
    }
}
