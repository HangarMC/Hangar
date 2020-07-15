package me.minidigger.hangar.model.db;


import java.time.LocalDateTime;

public class ProjectFlagsTable {

    private long id;
    private LocalDateTime createdAt;
    private long projectId;
    private long userId;
    private long reason;
    private boolean isResolved;
    private String comment;
    private LocalDateTime resolvedAt;
    private long resolvedBy;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }


    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
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


    public long getReason() {
        return reason;
    }

    public void setReason(long reason) {
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


    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }


    public long getResolvedBy() {
        return resolvedBy;
    }

    public void setResolvedBy(long resolvedBy) {
        this.resolvedBy = resolvedBy;
    }

}
