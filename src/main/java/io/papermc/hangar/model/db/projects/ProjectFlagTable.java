package io.papermc.hangar.model.db.projects;

import io.papermc.hangar.model.common.projects.FlagReason;
import io.papermc.hangar.model.db.Table;
import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

import java.time.OffsetDateTime;

public class ProjectFlagTable extends Table {

    private final long projectId;
    private final long userId;
    private final FlagReason reason;
    private boolean resolved;
    private final String comment;
    private OffsetDateTime resolvedAt;
    private long resolvedBy;

    @JdbiConstructor
    public ProjectFlagTable(OffsetDateTime createdAt, long id, long projectId, long userId, @EnumByOrdinal FlagReason reason, boolean resolved, String comment, OffsetDateTime resolvedAt, long resolvedBy) {
        super(createdAt, id);
        this.projectId = projectId;
        this.userId = userId;
        this.reason = reason;
        this.resolved = resolved;
        this.comment = comment;
        this.resolvedAt = resolvedAt;
        this.resolvedBy = resolvedBy;
    }

    public ProjectFlagTable(long projectId, long userId, FlagReason reason, String comment) {
        this.projectId = projectId;
        this.userId = userId;
        this.reason = reason;
        this.comment = comment;
    }

    public long getProjectId() {
        return projectId;
    }

    public long getUserId() {
        return userId;
    }

    @EnumByOrdinal
    public FlagReason getReason() {
        return reason;
    }

    public boolean isResolved() {
        return resolved;
    }

    public void setResolved(boolean resolved) {
        this.resolved = resolved;
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

    public long getResolvedBy() {
        return resolvedBy;
    }

    public void setResolvedBy(long resolvedBy) {
        this.resolvedBy = resolvedBy;
    }

    @Override
    public String toString() {
        return "ProjectFlagTable{" +
               "projectId=" + projectId +
               ", userId=" + userId +
               ", reason=" + reason +
               ", isResolved=" + resolved +
               ", comment='" + comment + '\'' +
               ", resolvedAt=" + resolvedAt +
               ", resolvedBy=" + resolvedBy +
               "} " + super.toString();
    }
}
