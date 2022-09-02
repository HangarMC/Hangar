package io.papermc.hangar.model.db.visibility;

import io.papermc.hangar.model.common.projects.Visibility;
import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

import java.time.OffsetDateTime;

public class ProjectVisibilityChangeTable extends VisibilityChangeTable {

    private final long projectId;

    @JdbiConstructor
    public ProjectVisibilityChangeTable(OffsetDateTime createdAt, long id, long createdBy, String comment, @EnumByOrdinal Visibility visibility, Long resolvedBy, OffsetDateTime resolvedAt, long projectId) {
        super(createdAt, id, createdBy, comment, visibility, resolvedBy, resolvedAt);
        this.projectId = projectId;
    }

    public ProjectVisibilityChangeTable(long createdBy, String comment, Visibility visibility, long projectId) {
        super(createdBy, comment, visibility);
        this.projectId = projectId;
    }

    public long getProjectId() {
        return projectId;
    }

    @Override
    public String toString() {
        return "ProjectVisibilityChangeTable{" +
                "projectId=" + projectId +
                "} " + super.toString();
    }
}
