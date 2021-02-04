package io.papermc.hangar.model.db.members;

public class ProjectMemberTable extends MemberTable {

    private final long projectId;

    public ProjectMemberTable(long userId, long projectId) {
        super(userId);
        this.projectId = projectId;
    }

    public long getProjectId() {
        return projectId;
    }
}
