package io.papermc.hangar.model.db.visibility;

import io.papermc.hangar.model.common.projects.Visibility;
import java.time.OffsetDateTime;
import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

public class ProjectVersionVisibilityChangeTable extends VisibilityChangeTable {

    private final long versionId;

    @JdbiConstructor
    public ProjectVersionVisibilityChangeTable(final OffsetDateTime createdAt, final long id, final long createdBy, final String comment, @EnumByOrdinal final Visibility visibility, final Long resolvedBy, final OffsetDateTime resolvedAt, final long versionId) {
        super(createdAt, id, createdBy, comment, visibility, resolvedBy, resolvedAt);
        this.versionId = versionId;
    }

    public ProjectVersionVisibilityChangeTable(final long createdBy, final String comment, final Visibility visibility, final long versionId) {
        super(createdBy, comment, visibility);
        this.versionId = versionId;
    }

    public long getVersionId() {
        return this.versionId;
    }

    @Override
    public String toString() {
        return "ProjectVersionVisibilityChangeTable{" +
            "versionId=" + this.versionId +
            "} " + super.toString();
    }
}
