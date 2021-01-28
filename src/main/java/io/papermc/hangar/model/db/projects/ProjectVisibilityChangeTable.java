package io.papermc.hangar.model.db.projects;

import io.papermc.hangar.model.Visibility;
import io.papermc.hangar.model.db.Table;
import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

import java.time.OffsetDateTime;

public class ProjectVisibilityChangeTable extends Table {

    private final long createdBy;
    private final long projectId;
    private final String comment;
    // TODO why are these resolvedAt/resolvedBy fields here? I don't see them being used anywhere, maybe can drop them.
    private OffsetDateTime resolvedAt;
    private Long resolvedBy;
    @EnumByOrdinal
    private final Visibility visibility;

    public ProjectVisibilityChangeTable(long createdBy, long projectId, String comment, Visibility visibility) {
        this.createdBy = createdBy;
        this.projectId = projectId;
        this.comment = comment;
        this.visibility = visibility;
    }

    @JdbiConstructor
    public ProjectVisibilityChangeTable(OffsetDateTime createdAt, long id, long createdBy, long projectId, String comment, OffsetDateTime resolvedAt, Long resolvedBy, @EnumByOrdinal Visibility visibility) {
        super(createdAt, id);
        this.createdBy = createdBy;
        this.projectId = projectId;
        this.comment = comment;
        this.resolvedAt = resolvedAt;
        this.resolvedBy = resolvedBy;
        this.visibility = visibility;
    }

    public long getCreatedBy() {
        return createdBy;
    }

    public long getProjectId() {
        return projectId;
    }

    public String getComment() {
        return comment;
    }

    public OffsetDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(OffsetDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public Long getResolvedBy() {
        return resolvedBy;
    }

    public void setResolvedBy(Long resolvedBy) {
        this.resolvedBy = resolvedBy;
    }

    public Visibility getVisibility() {
        return visibility;
    }
}
