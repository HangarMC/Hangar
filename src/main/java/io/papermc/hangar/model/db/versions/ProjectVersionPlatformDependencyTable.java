package io.papermc.hangar.model.db.versions;

import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

import java.time.OffsetDateTime;

public class ProjectVersionPlatformDependencyTable extends VersionDependencyTable {

    private final long platformVersionId;

    @JdbiConstructor
    public ProjectVersionPlatformDependencyTable(OffsetDateTime createdAt, long id, long versionId, long platformVersionId) {
        super(createdAt, id, versionId);
        this.platformVersionId = platformVersionId;
    }

    public ProjectVersionPlatformDependencyTable(long versionId, long platformVersionId) {
        super(versionId);
        this.platformVersionId = platformVersionId;
    }

    public long getPlatformVersionId() {
        return platformVersionId;
    }
}
