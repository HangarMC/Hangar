package io.papermc.hangar.model.db.visibility;

import io.papermc.hangar.model.common.projects.Visibility;
import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

import java.time.OffsetDateTime;

public class ProjectVersionVisibilityChangeTable extends VisibilityChangeTable {

    private final long versionId;

    @JdbiConstructor
    public ProjectVersionVisibilityChangeTable(OffsetDateTime createdAt, long id, long createdBy, String comment, @EnumByOrdinal Visibility visibility, Long resolvedBy, OffsetDateTime resolvedAt, long versionId) {
        super(createdAt, id, createdBy, comment, visibility, resolvedBy, resolvedAt);
        this.versionId = versionId;
    }

    public ProjectVersionVisibilityChangeTable(long createdBy, String comment, Visibility visibility, long versionId) {
        super(createdBy, comment, visibility);
        this.versionId = versionId;
    }

    public long getVersionId() {
        return versionId;
    }

    @Override
    public String toString() {
        return "ProjectVersionVisibilityChangeTable{" +
                "versionId=" + versionId +
                "} " + super.toString();
    }
}
