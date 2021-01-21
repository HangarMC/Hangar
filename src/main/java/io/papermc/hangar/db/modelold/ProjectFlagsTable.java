package io.papermc.hangar.db.modelold;


import io.papermc.hangar.modelold.FlagReason;
import org.jdbi.v3.core.enums.EnumByOrdinal;

import java.time.OffsetDateTime;

public class ProjectFlagsTable {

    private long id;
    private OffsetDateTime createdAt;
    private long projectId;
    private long userId;
    private FlagReason reason;
    private boolean isResolved;
    private String comment;
    private OffsetDateTime resolvedAt;
    private long resolvedBy;

    public ProjectFlagsTable(long projectId, long userId, FlagReason reason, String comment) {
        this.projectId = projectId;
        this.userId = userId;
        this.reason = reason;
        this.comment = comment;
    }

    public ProjectFlagsTable() { }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }


    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }


    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }


    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }


    @EnumByOrdinal
    public FlagReason getReason() {
        return reason;
    }

    @EnumByOrdinal
    public void setReason(FlagReason reason) {
        this.reason = reason;
    }


    public boolean getIsResolved() {
        return isResolved;
    }

    public void setIsResolved(boolean isResolved) {
        this.isResolved = isResolved;
    }


    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
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

}
