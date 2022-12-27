package io.papermc.hangar.model.db.projects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.papermc.hangar.model.Named;
import io.papermc.hangar.model.db.Table;
import io.papermc.hangar.model.identified.ProjectIdentified;
import java.time.OffsetDateTime;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

public class ProjectPageTable extends Table implements Named, ProjectIdentified {

    private final long projectId;
    private final String name;
    private final String slug;
    private String contents;
    private final boolean deletable;
    private Long parentId;

    @JdbiConstructor
    public ProjectPageTable(final OffsetDateTime createdAt, final long id, final long projectId, final String name, final String slug, final String contents, final boolean deletable, final Long parentId) {
        super(createdAt, id);
        this.projectId = projectId;
        this.name = name;
        this.slug = slug;
        this.contents = contents;
        this.deletable = deletable;
        this.parentId = parentId;
    }

    public ProjectPageTable(final long projectId, final String name, final String slug, final String contents, final boolean deletable, final Long parentId) {
        this.projectId = projectId;
        this.name = name;
        this.slug = slug;
        this.contents = contents;
        this.deletable = deletable;
        this.parentId = parentId;
    }

    @JsonIgnore
    @Override
    public long getProjectId() {
        return this.projectId;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public String getSlug() {
        return this.slug;
    }

    public String getContents() {
        return this.contents;
    }

    public void setContents(final String contents) {
        this.contents = contents;
    }

    public boolean isDeletable() {
        return this.deletable;
    }

    @JsonIgnore
    public Long getParentId() {
        return this.parentId;
    }

    public void setParentId(final Long parentId) {
        this.parentId = parentId;
    }

    @Override
    public String toString() {
        return "ProjectPageTable{" +
            "projectId=" + this.projectId +
            ", name='" + this.name + '\'' +
            ", slug='" + this.slug + '\'' +
            ", contents='" + this.contents + '\'' +
            ", deletable=" + this.deletable +
            ", parentId=" + this.parentId +
            "} " + super.toString();
    }
}
