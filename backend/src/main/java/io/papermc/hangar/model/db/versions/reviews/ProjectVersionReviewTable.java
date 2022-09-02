package io.papermc.hangar.model.db.versions.reviews;

import io.papermc.hangar.model.db.Table;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

import java.time.OffsetDateTime;

public class ProjectVersionReviewTable extends Table {

    private final long versionId;
    private final long userId;
    private OffsetDateTime endedAt;

    @JdbiConstructor
    public ProjectVersionReviewTable(OffsetDateTime createdAt, long id, long versionId, long userId, OffsetDateTime endedAt) {
        super(createdAt, id);
        this.versionId = versionId;
        this.userId = userId;
        this.endedAt = endedAt;
    }

    public ProjectVersionReviewTable(long versionId, long userId) {
        this.versionId = versionId;
        this.userId = userId;
    }

    public long getVersionId() {
        return versionId;
    }

    public long getUserId() {
        return userId;
    }

    public OffsetDateTime getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(OffsetDateTime endedAt) {
        this.endedAt = endedAt;
    }

    @Override
    public String toString() {
        return "ProjectVersionReviewTable{" +
                "versionId=" + versionId +
                ", userId=" + userId +
                ", endedAt=" + endedAt +
                "} " + super.toString();
    }
}
