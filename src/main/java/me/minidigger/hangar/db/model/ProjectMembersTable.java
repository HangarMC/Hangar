package me.minidigger.hangar.db.model;


public class ProjectMembersTable {

    private long projectId;
    private long userId;

    public ProjectMembersTable(long projectId, long userId) {
        this.projectId = projectId;
        this.userId = userId;
    }

    public ProjectMembersTable() {
        //
    }

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
