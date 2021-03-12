package io.papermc.hangar.db.modelold;


import io.papermc.hangar.db.customtypes.JSONB;

import java.time.OffsetDateTime;

@Deprecated(forRemoval = true)
public class ProjectVersionReviewsTable {

    private long id;
    private long versionId;
    private long userId;
    private OffsetDateTime createdAt;
    private OffsetDateTime endedAt;
    private JSONB comment;

    public ProjectVersionReviewsTable(ProjectVersionReviewsTable projectVersionReviewsTable) {
        this.id = projectVersionReviewsTable.id;
        this.versionId = projectVersionReviewsTable.versionId;
        this.userId = projectVersionReviewsTable.userId;
        this.createdAt = projectVersionReviewsTable.createdAt;
        this.endedAt = projectVersionReviewsTable.endedAt;
        this.comment = projectVersionReviewsTable.comment;
    }

    public ProjectVersionReviewsTable(long versionId, long userId, JSONB comment) {
        this.versionId = versionId;
        this.userId = userId;
        this.comment = comment;
    }

    public ProjectVersionReviewsTable() { }

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


    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }


    public OffsetDateTime getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(OffsetDateTime endedAt) {
        this.endedAt = endedAt;
    }


    public JSONB getComment() {
        return comment;
    }

    public void setComment(JSONB comment) {
        this.comment = comment;
    }

}
