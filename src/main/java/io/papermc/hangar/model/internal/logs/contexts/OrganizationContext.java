package io.papermc.hangar.model.internal.logs.contexts;

import io.papermc.hangar.model.db.log.LoggedActionsOrganizationTable;

public class OrganizationContext extends LogContext<LoggedActionsOrganizationTable, OrganizationContext> {

    private final Long organizationId;

    private OrganizationContext(Long organizationId) {
        super(Context.ORGANIZATION, LoggedActionsOrganizationTable::new);
        this.organizationId = organizationId;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public static OrganizationContext of(Long organizationId) {
        return new OrganizationContext(organizationId);
    }
}
