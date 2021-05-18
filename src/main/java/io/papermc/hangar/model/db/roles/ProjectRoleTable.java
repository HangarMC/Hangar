package io.papermc.hangar.model.db.roles;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.papermc.hangar.model.common.roles.ProjectRole;
import io.papermc.hangar.model.internal.logs.contexts.ProjectContext;
import io.papermc.hangar.model.loggable.ProjectLoggable;
import org.jdbi.v3.core.annotation.Unmappable;
import org.jdbi.v3.core.mapper.reflect.ColumnName;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

import java.time.OffsetDateTime;

public class ProjectRoleTable extends ExtendedRoleTable<ProjectRole, ProjectContext> implements ProjectLoggable {

    private final long projectId;

    @JdbiConstructor
    public ProjectRoleTable(OffsetDateTime createdAt, long id, long userId, @ColumnName("role_type") ProjectRole role, boolean accepted, long projectId) {
        super(createdAt, id, userId, role, accepted);
        this.projectId = projectId;
    }

    public ProjectRoleTable(long userId, ProjectRole role, boolean accepted, long projectId) {
        super(userId, role, accepted);
        this.projectId = projectId;
    }

    @JsonIgnore
    @Override
    public long getProjectId() {
        return projectId;
    }

    @Unmappable
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
