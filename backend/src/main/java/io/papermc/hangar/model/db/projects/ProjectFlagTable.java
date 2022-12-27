package io.papermc.hangar.model.db.projects;

import io.papermc.hangar.model.common.projects.FlagReason;
import io.papermc.hangar.model.db.Table;
import io.papermc.hangar.model.loggable.ProjectLoggable;
import java.time.OffsetDateTime;
import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

public class ProjectFlagTable extends Table implements ProjectLoggable {

    private final long projectId;
    private final long userId;
    private final FlagReason reason;
    private boolean resolved;
    private final String comment;
    private OffsetDateTime resolvedAt;
    private Long resolvedBy;

    @JdbiConstructor
    public ProjectFlagTable(final OffsetDateTime createdAt, final long id, final long projectId, final long userId, @EnumByOrdinal final FlagReason reason, final boolean resolved, final String comment, final OffsetDateTime resolvedAt, final Long resolvedBy) {
        super(createdAt, id);
        this.projectId = projectId;
        this.userId = userId;
        this.reason = reason;
        this.resolved = resolved;
        this.comment = comment;
        this.resolvedAt = resolvedAt;
        this.resolvedBy = resolvedBy;
    }

    public ProjectFlagTable(final long projectId, final long userId, final FlagReason reason, final String comment) {
        this.projectId = projectId;
        this.userId = userId;
        this.reason = reason;
        this.comment = comment;
    }

    public long getProjectId() {
        return this.projectId;
    }

    public long getUserId() {
        return this.userId;
    }

    @EnumByOrdinal
    public FlagReason getReason() {
        return this.reason;
    }

    public boolean isResolved() {
        return this.resolved;
    }

    public void setResolved(final boolean resolved) {
        this.resolved = resolved;
    }

    public String getComment() {
        return this.comment;
    }

    public OffsetDateTime getResolvedAt() {
        return this.resolvedAt;
    }

    public void setResolvedAt(final OffsetDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public Long getResolvedBy() {
        return this.resolvedBy;
    }

    public void setResolvedBy(final Long resolvedBy) {
        this.resolvedBy = resolvedBy;
    }

    @Override
    public String toString() {
        return "ProjectFlagTable{" +
            "projectId=" + this.projectId +
            ", userId=" + this.userId +
            ", reason=" + this.reason +
            ", resolved=" + this.resolved +
            ", comment='" + this.comment + '\'' +
            ", resolvedAt=" + this.resolvedAt +
            ", resolvedBy=" + this.resolvedBy +
            "} " + super.toString();
    }
}
