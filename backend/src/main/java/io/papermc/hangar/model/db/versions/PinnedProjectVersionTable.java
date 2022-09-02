package io.papermc.hangar.model.db.versions;

import io.papermc.hangar.model.db.Table;
import java.time.OffsetDateTime;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

public class PinnedProjectVersionTable extends Table {

    private final long projectId;
    private final long versionId;

    @JdbiConstructor
    public PinnedProjectVersionTable(final OffsetDateTime createdAt, final long id, final long projectId, final long versionId) {
        super(createdAt, id);
        this.projectId = projectId;
        this.versionId = versionId;
    }

    public PinnedProjectVersionTable(final long projectId, final long versionId) {
        this.projectId = projectId;
        this.versionId = versionId;
    }

    public long getProjectId() {
        return this.projectId;
    }

    public long getVersionId() {
        return this.versionId;
    }

    @Override
    public String toString() {
        return "PinnedProjectVersionTable{" +
            "projectId=" + this.projectId +
            ", versionId=" + this.versionId +
            "} " + super.toString();
    }
}
