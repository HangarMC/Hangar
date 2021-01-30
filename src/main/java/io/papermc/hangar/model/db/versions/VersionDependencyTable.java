package io.papermc.hangar.model.db.versions;

import io.papermc.hangar.model.db.Table;

import java.time.OffsetDateTime;

public abstract class VersionDependencyTable extends Table {

    private final long versionId;

    protected VersionDependencyTable(OffsetDateTime createdAt, long id, long versionId) {
        super(createdAt, id);
        this.versionId = versionId;
    }

    protected VersionDependencyTable(long versionId) {
        this.versionId = versionId;
    }

    public long getVersionId() {
        return versionId;
    }

    @Override
    public String toString() {
        return "VersionDependencyTable{" +
                "versionId=" + versionId +
                "} " + super.toString();
    }
}
