package io.papermc.hangar.model.db.versions.dependencies;

import java.time.OffsetDateTime;
import java.util.Objects;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

public class ProjectVersionPlatformDependencyTable extends VersionDependencyTable {

    private final long platformVersionId;

    @JdbiConstructor
    public ProjectVersionPlatformDependencyTable(final OffsetDateTime createdAt, final long id, final long versionId, final long platformVersionId) {
        super(createdAt, id, versionId);
        this.platformVersionId = platformVersionId;
    }

    public ProjectVersionPlatformDependencyTable(final long versionId, final long platformVersionId) {
        super(versionId);
        this.platformVersionId = platformVersionId;
    }

    public long getPlatformVersionId() {
        return this.platformVersionId;
    }

    @Override
    public String toString() {
        return "ProjectVersionPlatformDependencyTable{" +
            "platformVersionId=" + this.platformVersionId +
            "} " + super.toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        final ProjectVersionPlatformDependencyTable that = (ProjectVersionPlatformDependencyTable) o;
        return this.platformVersionId == that.platformVersionId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.platformVersionId);
    }
}
