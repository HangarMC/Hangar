package io.papermc.hangar.db.model;


public class ProjectWatchersTable {

    private long projectId;
    private long userId;


    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }


    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

}
