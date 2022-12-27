package io.papermc.hangar.model.db.visibility;

import io.papermc.hangar.model.common.projects.Visibility;
import java.time.OffsetDateTime;
import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

public class ProjectVisibilityChangeTable extends VisibilityChangeTable {

    private final long projectId;

    @JdbiConstructor
    public ProjectVisibilityChangeTable(final OffsetDateTime createdAt, final long id, final long createdBy, final String comment, @EnumByOrdinal final Visibility visibility, final Long resolvedBy, final OffsetDateTime resolvedAt, final long projectId) {
        super(createdAt, id, createdBy, comment, visibility, resolvedBy, resolvedAt);
        this.projectId = projectId;
    }

    public ProjectVisibilityChangeTable(final long createdBy, final String comment, final Visibility visibility, final long projectId) {
        super(createdBy, comment, visibility);
        this.projectId = projectId;
    }

    public long getProjectId() {
        return this.projectId;
    }

    @Override
    public String toString() {
        return "ProjectVisibilityChangeTable{" +
            "projectId=" + this.projectId +
            "} " + super.toString();
    }
}
