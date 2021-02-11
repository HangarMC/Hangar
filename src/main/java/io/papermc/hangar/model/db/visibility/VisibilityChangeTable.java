package io.papermc.hangar.model.db.visibility;

import io.papermc.hangar.model.common.projects.Visibility;
import io.papermc.hangar.model.db.Table;
import org.jdbi.v3.core.enums.EnumByOrdinal;

import java.time.OffsetDateTime;

public abstract class VisibilityChangeTable extends Table {

    private final long createdBy;
    private final String comment;
    private final Visibility visibility;
    private Long resolvedBy;
    private OffsetDateTime resolvedAt;

    public VisibilityChangeTable(OffsetDateTime createdAt, long id, long createdBy, String comment, Visibility visibility, Long resolvedBy, OffsetDateTime resolvedAt) {
        super(createdAt, id);
        this.createdBy = createdBy;
        this.comment = comment;
        this.visibility = visibility;
        this.resolvedBy = resolvedBy;
        this.resolvedAt = resolvedAt;
    }

    public VisibilityChangeTable(long createdBy, String comment, Visibility visibility) {
        this.createdBy = createdBy;
        this.comment = comment;
        this.visibility = visibility;
    }

    public long getCreatedBy() {
        return createdBy;
    }

    public String getComment() {
        return comment;
    }

    @EnumByOrdinal
    public Visibility getVisibility() {
        return visibility;
    }

    public Long getResolvedBy() {
        return resolvedBy;
    }

    public OffsetDateTime getResolvedAt() {
        return resolvedAt;
    }

    @Override
    public String toString() {
        return "VisibilityChangeTable{" +
                "createdBy=" + createdBy +
                ", comment='" + comment + '\'' +
                ", visibility=" + visibility +
                ", resolvedBy=" + resolvedBy +
                ", resolvedAt=" + resolvedAt +
                "} " + super.toString();
    }
}
