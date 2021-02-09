package io.papermc.hangar.model.db.log;


import io.papermc.hangar.db.customtypes.LoggedAction;

import java.net.InetAddress;

public class LoggedActionsOrganizationTable extends LoggedActionTable {

    private final long organizationId;

    public LoggedActionsOrganizationTable(long userId, InetAddress address, LoggedAction action, String newState, String oldState, long organizationId) {
        super(userId, address, action, newState, oldState);
        this.organizationId = organizationId;
    }

    public long getOrganizationId() {
        return organizationId;
    }

    @Override
    public String toString() {
        return "LoggedActionsOrganizationTable{" +
                "organizationId=" + organizationId +
                "} " + super.toString();
    }
}
