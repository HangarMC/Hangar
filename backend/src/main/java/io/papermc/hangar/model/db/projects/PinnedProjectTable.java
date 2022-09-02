package io.papermc.hangar.model.db.projects;

import io.papermc.hangar.model.db.Table;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

public class PinnedProjectTable extends Table {

    private final long projectId;
    private final long userId;

    @JdbiConstructor
    public PinnedProjectTable(final long id, final long projectId, final long userId) {
        super(id);
        this.projectId = projectId;
        this.userId = userId;
    }

    public PinnedProjectTable(final long projectId, final long userId) {
        this.projectId = projectId;
        this.userId = userId;
    }

    public long getProjectId() {
        return this.projectId;
    }

    public long getVersionId() {
        return this.userId;
    }

    @Override
    public String toString() {
        return "PinnedProjectTable{" +
            "projectId=" + this.projectId +
            ", userId=" + this.userId +
            "} " + super.toString();
    }
}
