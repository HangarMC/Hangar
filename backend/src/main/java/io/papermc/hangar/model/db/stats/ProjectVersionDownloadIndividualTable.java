package io.papermc.hangar.model.db.stats;

import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.model.identified.ProjectIdentified;
import io.papermc.hangar.model.identified.VersionIdentified;
import java.net.InetAddress;
import java.time.OffsetDateTime;

import java.util.UUID;
import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

public class ProjectVersionDownloadIndividualTable extends IndividualTable implements ProjectIdentified, VersionIdentified {

    private final long projectId;
    private final long versionId;
    private final Platform platform;

    public ProjectVersionDownloadIndividualTable(final InetAddress address, final UUID cookie, final Long userId, final long projectId, final long versionId, final Platform platform) {
        super(address, cookie, userId);
        this.projectId = projectId;
        this.versionId = versionId;
        this.platform = platform;
    }

    @JdbiConstructor
    public ProjectVersionDownloadIndividualTable(final OffsetDateTime createdAt, final long id, final InetAddress address, final UUID cookie, final Long userId, final int processed, final long projectId, final long versionId, final Platform platform) {
        super(createdAt, id, address, cookie, userId, processed);
        this.projectId = projectId;
        this.versionId = versionId;
        this.platform = platform;
    }

    @Override
    public long getProjectId() {
        return this.projectId;
    }

    @Override
    public long getVersionId() {
        return this.versionId;
    }

    @EnumByOrdinal
    public Platform getPlatform() {
        return this.platform;
    }

    @Override
    public String toString() {
        return "ProjectVersionDownloadIndividualTable{" +
            "projectId=" + this.projectId +
            ", versionId=" + this.versionId +
            ", platform=" + this.platform +
            "} " + super.toString();
    }
}
