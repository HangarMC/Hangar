package io.papermc.hangar.model.db.versions;

import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.model.db.Table;
import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

import java.time.OffsetDateTime;

public class RecommendedProjectVersionTable extends Table {

    private final long versionId;
    private final long projectId;
    private final Platform platform;

    @JdbiConstructor
    public RecommendedProjectVersionTable(OffsetDateTime createdAt, long id, long versionId, long projectId, @EnumByOrdinal Platform platform) {
        super(createdAt, id);
        this.versionId = versionId;
        this.projectId = projectId;
        this.platform = platform;
    }

    public RecommendedProjectVersionTable(long versionId, long projectId, Platform platform) {
        this.versionId = versionId;
        this.projectId = projectId;
        this.platform = platform;
    }

    public long getVersionId() {
        return versionId;
    }

    public long getProjectId() {
        return projectId;
    }

    @EnumByOrdinal
    public Platform getPlatform() {
        return platform;
    }

    @Override
    public String toString() {
        return "RecommendedProjectVersionTable{" +
                "versionId=" + versionId +
                ", projectId=" + projectId +
                ", platform=" + platform +
                "} " + super.toString();
    }
}
