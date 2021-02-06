package io.papermc.hangar.db.modelold;

import java.time.OffsetDateTime;

@Deprecated(forRemoval = true)
public class ProjectPagesTable {

    private long id;
    private OffsetDateTime createdAt;
    private long projectId;
    private String name;
    private String slug;
    private String contents;
    private boolean deletable;
    private Long parentId;

    public ProjectPagesTable(long projectId, String name, String slug, String contents, boolean deletable, Long parentId) {
        this.projectId = projectId;
        this.name = name;
        this.slug = slug;
        this.contents = contents;
        this.deletable = deletable;
        this.parentId = parentId;
    }

    public ProjectPagesTable() {
        //
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }


    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }


    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
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

    public void setDeletable(boolean isDeletable) {
        this.deletable = isDeletable;
    }


    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

}
