package io.papermc.hangar.model.db.log;

import io.papermc.hangar.db.customtypes.LoggedAction;

import java.net.InetAddress;

public class LoggedActionsProjectTable extends LoggedActionTable {

    private final long projectId;

    public LoggedActionsProjectTable(long userId, InetAddress address, LoggedAction action, String newState, String oldState, long projectId) {
        super(userId, address, action, newState, oldState);
        this.projectId = projectId;
    }

    public long getProjectId() {
        return projectId;
    }

    @Override
    public String toString() {
        return "LoggedActionsProjectTable{" +
                "projectId=" + projectId +
                "} " + super.toString();
    }
}
