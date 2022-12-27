package io.papermc.hangar.model.db.stats;

import io.papermc.hangar.model.identified.ProjectIdentified;
import io.papermc.hangar.model.identified.VersionIdentified;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

import java.net.InetAddress;
import java.time.OffsetDateTime;

public class ProjectVersionDownloadIndividualTable extends IndividualTable implements ProjectIdentified, VersionIdentified {

    private final long projectId;
    private final long versionId;

    public ProjectVersionDownloadIndividualTable(final InetAddress address, final String cookie, final Long userId, final long projectId, final long versionId) {
        super(address, cookie, userId);
        this.projectId = projectId;
        this.versionId = versionId;
    }

    @JdbiConstructor
    public ProjectVersionDownloadIndividualTable(final OffsetDateTime createdAt, final long id, final InetAddress address, final String cookie, final Long userId, final int processed, final long projectId, final long versionId) {
        super(createdAt, id, address, cookie, userId, processed);
        this.projectId = projectId;
        this.versionId = versionId;
    }

    @Override
    public long getProjectId() {
        return this.projectId;
    }

    @Override
    public long getVersionId() {
        return this.versionId;
    }

    @Override
    public String toString() {
        return "ProjectVersionDownloadIndividualTable{" +
                "projectId=" + this.projectId +
                ", versionId=" + this.versionId +
                "} " + super.toString();
    }
}
