package io.papermc.hangar.model.db.stats;

import io.papermc.hangar.model.ProjectIdentified;
import io.papermc.hangar.model.VersionIdentified;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

import java.net.InetAddress;
import java.time.OffsetDateTime;

public class ProjectVersionDownloadIndividualTable extends IndividualTable implements ProjectIdentified, VersionIdentified {

    private final long projectId;
    private final long versionId;

    public ProjectVersionDownloadIndividualTable(InetAddress address, String cookie, Long userId, long projectId, long versionId) {
        super(address, cookie, userId);
        this.projectId = projectId;
        this.versionId = versionId;
    }

    @JdbiConstructor
    public ProjectVersionDownloadIndividualTable(OffsetDateTime createdAt, long id, InetAddress address, String cookie, Long userId, int processed, long projectId, long versionId) {
        super(createdAt, id, address, cookie, userId, processed);
        this.projectId = projectId;
        this.versionId = versionId;
    }

    @Override
    public long getProjectId() {
        return projectId;
    }

    @Override
    public long getVersionId() {
        return versionId;
    }

    @Override
    public String toString() {
        return "ProjectVersionDownloadIndividualTable{" +
                "projectId=" + projectId +
                ", versionId=" + versionId +
                "} " + super.toString();
    }
}
