package io.papermc.hangar.model.db.log;


import io.papermc.hangar.model.internal.logs.LoggedAction;
import io.papermc.hangar.model.internal.logs.contexts.OrganizationContext;
import java.net.InetAddress;

public class LoggedActionsOrganizationTable extends LoggedActionTable {

    private final long organizationId;

    public LoggedActionsOrganizationTable(final long userId, final InetAddress address, final LoggedAction<OrganizationContext> action) {
        super(userId, address, action);
        this.organizationId = action.getContext().getOrganizationId();
    }

    public long getOrganizationId() {
        return this.organizationId;
    }

    @Override
    public String toString() {
        return "LoggedActionsOrganizationTable{" +
            "organizationId=" + this.organizationId +
            "} " + super.toString();
    }
}
