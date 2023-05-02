package io.papermc.hangar.model.db.stats;

import io.papermc.hangar.model.identified.ProjectIdentified;
import java.net.InetAddress;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

public class ProjectViewIndividualTable extends IndividualTable implements ProjectIdentified {

    private final long projectId;

    public ProjectViewIndividualTable(final InetAddress address, final UUID cookie, final Long userId, final long projectId) {
        super(address, cookie, userId);
        this.projectId = projectId;
    }

    @JdbiConstructor
    public ProjectViewIndividualTable(final OffsetDateTime createdAt, final long id, final long projectId, final InetAddress address, final UUID cookie, final Long userId, final int processed) {
        super(createdAt, id, address, cookie, userId, processed);
        this.projectId = projectId;
    }

    @Override
    public long getProjectId() {
        return this.projectId;
    }

    @Override
    public String toString() {
        return "ProjectViewIndividualTable{" +
            "projectId=" + this.projectId +
            "} " + super.toString();
    }
}
