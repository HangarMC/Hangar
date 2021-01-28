package io.papermc.hangar.model.db.roles;

import io.papermc.hangar.model.Role;

import java.time.OffsetDateTime;

public class ProjectRoleTable extends RoleTable {

    private final long projectId;

    public ProjectRoleTable(OffsetDateTime createdAt, long id, long userId, Role role, boolean isAccepted, long projectId) {
        super(createdAt, id, userId, role, isAccepted);
        this.projectId = projectId;
    }

    public ProjectRoleTable(long userId, Role role, boolean isAccepted, long projectId) {
        super(userId, role, isAccepted);
        this.projectId = projectId;
    }

    public long getProjectId() {
        return projectId;
    }
}
