package io.papermc.hangar.model.db.log;


import io.papermc.hangar.db.customtypes.LoggedAction;

import java.net.InetAddress;

public class LoggedActionsPageTable extends LoggedActionTable {

    private final long projectId;
    private final long pageId;

    public LoggedActionsPageTable(long userId, InetAddress address, LoggedAction action, String newState, String oldState, long projectId, long pageId) {
        super(userId, address, action, newState, oldState);
        this.projectId = projectId;
        this.pageId = pageId;
    }

    public long getProjectId() {
        return projectId;
    }

    public long getPageId() {
        return pageId;
    }

    @Override
    public String toString() {
        return "LoggedActionsPageTable{" +
                "projectId=" + projectId +
                ", pageId=" + pageId +
                "} " + super.toString();
    }
}
