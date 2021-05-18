package io.papermc.hangar.model.db.roles;


import com.fasterxml.jackson.annotation.JsonIgnore;
import io.papermc.hangar.model.common.roles.OrganizationRole;
import io.papermc.hangar.model.internal.logs.LoggedAction;
import io.papermc.hangar.model.internal.logs.contexts.OrganizationContext;
import io.papermc.hangar.service.internal.UserActionLogService;
import org.jdbi.v3.core.annotation.Unmappable;
import org.jdbi.v3.core.mapper.reflect.ColumnName;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

import java.time.OffsetDateTime;
import java.util.function.Consumer;

public class OrganizationRoleTable extends ExtendedRoleTable<OrganizationRole, OrganizationContext> {

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

    @JsonIgnore
    public long getOrganizationId() {
        return organizationId;
    }

    @Override
    @Unmappable
    public long getPrincipalId() {
        return organizationId;
    }

    @Override
    public Consumer<LoggedAction<OrganizationContext>> getLogInserter(UserActionLogService actionLogger) {
        return actionLogger::organization;
    }

    @Override
    public OrganizationContext createLogContext() {
        return OrganizationContext.of(this.organizationId);
    }

    @Override
    public String toString() {
        return "OrganizationRoleTable{" +
                "organizationId=" + organizationId +
                "} " + super.toString();
    }
}
