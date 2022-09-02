package io.papermc.hangar.model.db.log;

import io.papermc.hangar.model.internal.logs.LoggedAction;
import io.papermc.hangar.model.internal.logs.contexts.VersionContext;

import java.net.InetAddress;

public class LoggedActionsVersionTable extends LoggedActionTable {

    private final long projectId;
    private final long versionId;

    public LoggedActionsVersionTable(long userId, InetAddress address, LoggedAction<VersionContext> action) {
        super(userId, address, action);
        this.projectId = action.getContext().getProjectId();
        this.versionId = action.getContext().getVersionId();
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
