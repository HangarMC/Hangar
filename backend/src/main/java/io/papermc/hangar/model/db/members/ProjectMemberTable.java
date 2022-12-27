package io.papermc.hangar.model.db.members;

public class ProjectMemberTable extends MemberTable {

    private final long projectId;

    public ProjectMemberTable(final long userId, final long projectId) {
        super(userId);
        this.projectId = projectId;
    }

    public long getProjectId() {
        return this.projectId;
    }
}
