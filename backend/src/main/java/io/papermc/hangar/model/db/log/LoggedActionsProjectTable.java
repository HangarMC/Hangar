package io.papermc.hangar.model.db.log;

import io.papermc.hangar.model.internal.logs.LoggedAction;
import io.papermc.hangar.model.internal.logs.contexts.ProjectContext;
import java.net.InetAddress;

public class LoggedActionsProjectTable extends LoggedActionTable {

    private final long projectId;

    public LoggedActionsProjectTable(final long userId, final InetAddress address, final LoggedAction<ProjectContext> action) {
        super(userId, address, action);
        this.projectId = action.getContext().getProjectId();
    }

    public long getProjectId() {
        return this.projectId;
    }

    @Override
    public String toString() {
        return "LoggedActionsProjectTable{" +
            "projectId=" + this.projectId +
            "} " + super.toString();
    }
}
