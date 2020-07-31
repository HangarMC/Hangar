package me.minidigger.hangar.db.model;


import org.springframework.lang.Nullable;

import java.time.OffsetDateTime;

public class ProjectVisibilityChangesTable {

    private long id;
    private OffsetDateTime createdAt;
    private long createdBy;
    private long projectId;
    private String comment;
    @Nullable
    private OffsetDateTime resolvedAt;
    @Nullable
    private Long resolvedBy;
    private long visibility;

    public ProjectVisibilityChangesTable(long createdBy, long projectId, String comment, @Nullable OffsetDateTime resolvedAt, @Nullable Long resolvedBy, long visibility) {
        this.createdBy = createdBy;
        this.projectId = projectId;
        this.comment = comment;
        this.resolvedAt = resolvedAt;
        this.resolvedBy = resolvedBy;
        this.visibility = visibility;
    }

    public ProjectVisibilityChangesTable() { }

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


    public long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(long createdBy) {
        this.createdBy = createdBy;
    }


    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }


    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }


    @Nullable
    public OffsetDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(@Nullable OffsetDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }


    @Nullable
    public Long getResolvedBy() {
        return resolvedBy;
    }

    public void setResolvedBy(long resolvedBy) {
        this.resolvedBy = resolvedBy;
    }


    public long getVisibility() {
        return visibility;
    }

    public void setVisibility(long visibility) {
        this.visibility = visibility;
    }

}
