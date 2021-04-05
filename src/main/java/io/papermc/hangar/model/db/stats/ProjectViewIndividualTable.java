package io.papermc.hangar.model.db.stats;

import io.papermc.hangar.model.db.ProjectIdentified;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

import java.net.InetAddress;
import java.time.OffsetDateTime;

public class ProjectViewIndividualTable extends IndividualTable implements ProjectIdentified {

    private final long projectId;

    public ProjectViewIndividualTable(InetAddress address, String cookie, Long userId, long projectId) {
        super(address, cookie, userId);
        this.projectId = projectId;
    }

    @JdbiConstructor
    public ProjectViewIndividualTable(OffsetDateTime createdAt, long id, long projectId, InetAddress address, String cookie, Long userId, int processed) {
        super(createdAt, id, address, cookie, userId, processed);
        this.projectId = projectId;
    }

    @Override
    public long getProjectId() {
        return projectId;
    }

    @Override
    public String toString() {
        return "ProjectViewIndividualTable{" +
                "projectId=" + projectId +
                "} " + super.toString();
    }
}
