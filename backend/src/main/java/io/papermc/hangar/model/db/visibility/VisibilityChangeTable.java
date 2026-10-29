package io.papermc.hangar.model.db.visibility;

import io.papermc.hangar.model.common.projects.Visibility;
import io.papermc.hangar.model.db.Table;
import java.time.OffsetDateTime;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jdbi.v3.core.enums.EnumByOrdinal;

public abstract class VisibilityChangeTable extends Table {

    private final @Nullable Long createdBy;
    private final String comment;
    private final Visibility visibility;
    private Long resolvedBy;
    private OffsetDateTime resolvedAt;

    protected VisibilityChangeTable(final OffsetDateTime createdAt, final long id, final @Nullable Long createdBy, final String comment, final Visibility visibility, final Long resolvedBy, final OffsetDateTime resolvedAt) {
        super(createdAt, id);
        this.createdBy = createdBy;
        this.comment = comment;
        this.visibility = visibility;
        this.resolvedBy = resolvedBy;
        this.resolvedAt = resolvedAt;
    }

    protected VisibilityChangeTable(final long createdBy, final String comment, final Visibility visibility) {
        this.createdBy = createdBy;
        this.comment = comment;
        this.visibility = visibility;
    }

    public @Nullable Long getCreatedBy() {
        return this.createdBy;
    }

    public String getComment() {
        return this.comment;
    }

    @EnumByOrdinal
    public Visibility getVisibility() {
        return this.visibility;
    }

    public Long getResolvedBy() {
        return this.resolvedBy;
    }

    public OffsetDateTime getResolvedAt() {
        return this.resolvedAt;
    }

    @Override
    public String toString() {
        return "VisibilityChangeTable{" +
            "createdBy=" + this.createdBy +
            ", comment='" + this.comment + '\'' +
            ", visibility=" + this.visibility +
            ", resolvedBy=" + this.resolvedBy +
            ", resolvedAt=" + this.resolvedAt +
            "} " + super.toString();
    }
}
