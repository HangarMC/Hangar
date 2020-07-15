package me.minidigger.hangar.model.db;


import java.time.LocalDateTime;

public class ProjectVersionVisibilityChangesTable {

    private long id;
    private LocalDateTime createdAt;
    private long createdBy;
    private long versionId;
    private String comment;
    private LocalDateTime resolvedAt;
    private long resolvedBy;
    private long visibility;


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


    public long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(long createdBy) {
        this.createdBy = createdBy;
    }


    public long getVersionId() {
        return versionId;
    }

    public void setVersionId(long versionId) {
        this.versionId = versionId;
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


    public long getVisibility() {
        return visibility;
    }

    public void setVisibility(long visibility) {
        this.visibility = visibility;
    }

}
