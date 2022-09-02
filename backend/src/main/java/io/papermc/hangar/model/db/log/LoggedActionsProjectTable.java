package io.papermc.hangar.model.db.log;

import io.papermc.hangar.model.internal.logs.LoggedAction;
import io.papermc.hangar.model.internal.logs.contexts.ProjectContext;

import java.net.InetAddress;

public class LoggedActionsProjectTable extends LoggedActionTable {

    private final long projectId;

    public LoggedActionsProjectTable(long userId, InetAddress address, LoggedAction<ProjectContext> action) {
        super(userId, address, action);
        this.projectId = action.getContext().getProjectId();
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
