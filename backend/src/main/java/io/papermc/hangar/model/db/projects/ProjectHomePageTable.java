package io.papermc.hangar.model.db.projects;

import io.papermc.hangar.model.db.Table;
import java.time.OffsetDateTime;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

public class ProjectHomePageTable extends Table {

    private final long projectId;
    private final long pageId;

    public ProjectHomePageTable(final long projectId, final long pageId) {
        this.projectId = projectId;
        this.pageId = pageId;
    }

    @JdbiConstructor
    public ProjectHomePageTable(final OffsetDateTime createdAt, final long id, final long projectId, final long pageId) {
        super(createdAt, id);
        this.projectId = projectId;
        this.pageId = pageId;
    }

    public long getProjectId() {
        return this.projectId;
    }

    public long getPageId() {
        return this.pageId;
    }

    @Override
    public String toString() {
        return "ProjectHomePageTable{" +
            "projectId=" + this.projectId +
            ", pageId=" + this.pageId +
            "} " + super.toString();
    }
}
