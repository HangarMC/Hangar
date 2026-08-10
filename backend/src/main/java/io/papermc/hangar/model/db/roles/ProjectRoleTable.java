package io.papermc.hangar.model.db.roles;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.papermc.hangar.model.common.Permission;
import io.papermc.hangar.model.internal.logs.contexts.ProjectContext;
import io.papermc.hangar.model.loggable.ProjectLoggable;
import java.time.OffsetDateTime;
import org.jdbi.v3.core.annotation.JdbiProperty;
import org.jdbi.v3.core.mapper.reflect.ColumnName;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

public class ProjectRoleTable extends ExtendedRoleTable<ProjectContext> implements ProjectLoggable {

    private final long projectId;

    @JdbiConstructor
    public ProjectRoleTable(final OffsetDateTime createdAt, final long id, final long userId, final Permission permissions, final String title, final boolean accepted, @ColumnName("is_owner") final boolean owner, final long projectId) {
        super(createdAt, id, userId, permissions, title, accepted, owner);
        this.projectId = projectId;
    }

    public ProjectRoleTable(final long userId, final Permission permissions, final String title, final boolean accepted, final boolean owner, final long projectId) {
        super(userId, permissions, title, accepted, owner);
        this.projectId = projectId;
    }

    @JsonIgnore
    @Override
    public long getProjectId() {
        return this.projectId;
    }

    @JdbiProperty(map = false)
    @Override
    public long getPrincipalId() {
        return this.projectId;
    }

    @Override
    public String toString() {
        return "ProjectRoleTable{" +
            "projectId=" + this.projectId +
            "} " + super.toString();
    }
}
