package io.papermc.hangar.model.db.roles;

import io.papermc.hangar.model.roles.ProjectRole;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

import java.time.OffsetDateTime;

public class ProjectRoleTable extends ExtendedRoleTable<ProjectRole> {

    private final long projectId;

    @JdbiConstructor
    public ProjectRoleTable(OffsetDateTime createdAt, long id, long userId, ProjectRole role, boolean accepted, long projectId) {
        super(createdAt, id, userId, role, accepted);
        this.projectId = projectId;
    }

    public ProjectRoleTable(long userId, ProjectRole role, boolean accepted, long projectId) {
        super(userId, role, accepted);
        this.projectId = projectId;
    }

    public long getProjectId() {
        return projectId;
    }

    @Override
    public long getPrincipalId() {
        return projectId;
    }

    @Override
    public String toString() {
        return "ProjectRoleTable{" +
                "projectId=" + projectId +
                "} " + super.toString();
    }
}
