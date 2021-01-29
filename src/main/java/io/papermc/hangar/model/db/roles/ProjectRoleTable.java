package io.papermc.hangar.model.db.roles;

import io.papermc.hangar.model.roles.ProjectRole;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

import java.time.OffsetDateTime;

public class ProjectRoleTable extends ExtendedRoleTable<ProjectRole> {

    private final long projectId;

    @JdbiConstructor
    public ProjectRoleTable(OffsetDateTime createdAt, long id, long userId, ProjectRole role, boolean isAccepted, long projectId) {
        super(createdAt, id, userId, role, isAccepted);
        this.projectId = projectId;
    }

    public ProjectRoleTable(long userId, ProjectRole role, boolean isAccepted, long projectId) {
        super(userId, role, isAccepted);
        this.projectId = projectId;
    }

    public long getProjectId() {
        return projectId;
    }
}
