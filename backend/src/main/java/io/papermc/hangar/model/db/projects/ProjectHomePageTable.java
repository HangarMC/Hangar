package io.papermc.hangar.model.db.projects;

import io.papermc.hangar.model.db.Table;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

import java.time.OffsetDateTime;

public class ProjectHomePageTable extends Table {

    private final long projectId;
    private final long pageId;

    public ProjectHomePageTable(long projectId, long pageId) {
        this.projectId = projectId;
        this.pageId = pageId;
    }

    @JdbiConstructor
    public ProjectHomePageTable(OffsetDateTime createdAt, long id, long projectId, long pageId) {
        super(createdAt, id);
        this.projectId = projectId;
        this.pageId = pageId;
    }

    public long getProjectId() {
        return projectId;
    }

    public long getPageId() {
        return pageId;
    }

    @Override
    public String toString() {
        return "ProjectHomePageTable{" +
                "projectId=" + projectId +
                ", pageId=" + pageId +
                "} " + super.toString();
    }
}
