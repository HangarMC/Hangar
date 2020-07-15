package me.minidigger.hangar.model.db;


import java.time.LocalDateTime;

import me.minidigger.hangar.model.db.customtypes.JSONB;

public class ProjectVersionReviewsTable {

    private long id;
    private long versionId;
    private long userId;
    private LocalDateTime createdAt;
    private LocalDateTime endedAt;
    private JSONB comment;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }


    public long getVersionId() {
        return versionId;
    }

    public void setVersionId(long versionId) {
        this.versionId = versionId;
    }


    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }


    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }


    public LocalDateTime getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(LocalDateTime endedAt) {
        this.endedAt = endedAt;
    }


    public JSONB getComment() {
        return comment;
    }

    public void setComment(JSONB comment) {
        this.comment = comment;
    }

}
