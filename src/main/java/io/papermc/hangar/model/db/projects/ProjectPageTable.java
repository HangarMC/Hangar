package io.papermc.hangar.model.db.projects;

import io.papermc.hangar.model.Named;
import io.papermc.hangar.model.db.Table;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

import java.time.OffsetDateTime;

public class ProjectPageTable extends Table implements Named {

    private final long projectId;
    private final String name;
    private final String slug;
    private String contents;
    private final boolean deletable;
    private final Long parentId;

    @JdbiConstructor
    public ProjectPageTable(OffsetDateTime createdAt, long id, long projectId, String name, String slug, String contents, boolean deletable, Long parentId) {
        super(createdAt, id);
        this.projectId = projectId;
        this.name = name;
        this.slug = slug;
        this.contents = contents;
        this.deletable = deletable;
        this.parentId = parentId;
    }

    public ProjectPageTable(long projectId, String name, String slug, String contents, boolean deletable, Long parentId) {
        this.projectId = projectId;
        this.name = name;
        this.slug = slug;
        this.contents = contents;
        this.deletable = deletable;
        this.parentId = parentId;
    }

    public long getProjectId() {
        return projectId;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getSlug() {
        return slug;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public boolean isDeletable() {
        return deletable;
    }

    public Long getParentId() {
        return parentId;
    }

    @Override
    public String toString() {
        return "ProjectPageTable{" +
                "projectId=" + projectId +
                ", name='" + name + '\'' +
                ", slug='" + slug + '\'' +
                ", contents='" + contents + '\'' +
                ", deletable=" + deletable +
                ", parentId=" + parentId +
                "} " + super.toString();
    }
}
