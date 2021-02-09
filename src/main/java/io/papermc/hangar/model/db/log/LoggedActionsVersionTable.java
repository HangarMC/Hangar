package io.papermc.hangar.model.db.log;

import io.papermc.hangar.db.customtypes.LoggedAction;

import java.net.InetAddress;

public class LoggedActionsVersionTable extends LoggedActionTable {

    private final long projectId;
    private final long versionId;

    public LoggedActionsVersionTable(long userId, InetAddress address, LoggedAction action, String newState, String oldState, long projectId, long versionId) {
        super(userId, address, action, newState, oldState);
        this.projectId = projectId;
        this.versionId = versionId;
    }

    public long getProjectId() {
        return projectId;
    }

    public long getVersionId() {
        return versionId;
    }

    @Override
    public String toString() {
        return "LoggedActionsVersionTable{" +
                "projectId=" + projectId +
                ", versionId=" + versionId +
                "} " + super.toString();
    }
}
