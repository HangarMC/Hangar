package io.papermc.hangar.model.db.versions.dependencies;

import io.papermc.hangar.model.db.Table;
import java.time.OffsetDateTime;

public abstract class VersionDependencyTable extends Table {

    private final long versionId;

    protected VersionDependencyTable(final OffsetDateTime createdAt, final long id, final long versionId) {
        super(createdAt, id);
        this.versionId = versionId;
    }

    protected VersionDependencyTable(final long versionId) {
        this.versionId = versionId;
    }

    public long getVersionId() {
        return this.versionId;
    }

    @Override
    public String toString() {
        return "VersionDependencyTable{" +
            "versionId=" + this.versionId +
            "} " + super.toString();
    }
}
