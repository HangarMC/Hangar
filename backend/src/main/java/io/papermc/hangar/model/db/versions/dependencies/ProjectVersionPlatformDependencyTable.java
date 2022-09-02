package io.papermc.hangar.model.db.versions.dependencies;

import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

import java.time.OffsetDateTime;
import java.util.Objects;

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

    @Override
    public String toString() {
        return "ProjectVersionPlatformDependencyTable{" +
                "platformVersionId=" + platformVersionId +
                "} " + super.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ProjectVersionPlatformDependencyTable that = (ProjectVersionPlatformDependencyTable) o;
        return platformVersionId == that.platformVersionId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), platformVersionId);
    }
}
