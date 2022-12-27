package io.papermc.hangar.model.db.versions.reviews;

import io.papermc.hangar.model.db.Table;
import java.time.OffsetDateTime;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

public class ProjectVersionReviewTable extends Table {

    private final long versionId;
    private final long userId;
    private OffsetDateTime endedAt;

    @JdbiConstructor
    public ProjectVersionReviewTable(final OffsetDateTime createdAt, final long id, final long versionId, final long userId, final OffsetDateTime endedAt) {
        super(createdAt, id);
        this.versionId = versionId;
        this.userId = userId;
        this.endedAt = endedAt;
    }

    public ProjectVersionReviewTable(final long versionId, final long userId) {
        this.versionId = versionId;
        this.userId = userId;
    }

    public long getVersionId() {
        return this.versionId;
    }

    public long getUserId() {
        return this.userId;
    }

    public OffsetDateTime getEndedAt() {
        return this.endedAt;
    }

    public void setEndedAt(final OffsetDateTime endedAt) {
        this.endedAt = endedAt;
    }

    @Override
    public String toString() {
        return "ProjectVersionReviewTable{" +
            "versionId=" + this.versionId +
            ", userId=" + this.userId +
            ", endedAt=" + this.endedAt +
            "} " + super.toString();
    }
}
