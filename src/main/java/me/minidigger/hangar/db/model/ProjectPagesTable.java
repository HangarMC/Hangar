package me.minidigger.hangar.db.model;


import java.time.OffsetDateTime;

public class ProjectPagesTable {

    private long id;
    private OffsetDateTime createdAt;
    private long projectId;
    private String name;
    private String slug;
    private String contents;
    private boolean isDeletable;
    private Long parentId;

    public ProjectPagesTable(long id, OffsetDateTime createdAt, long projectId, String name, String slug, String contents, boolean isDeletable, Long parentId) {
        this.id = id;
        this.createdAt = createdAt;
        this.projectId = projectId;
        this.name = name;
        this.slug = slug;
        this.contents = contents;
        this.isDeletable = isDeletable;
        this.parentId = parentId;
    }

    public ProjectPagesTable() {
        //
    }

    public String getFullSlug(ProjectPagesTable parentTable) {
        if (parentTable != null) {
            return parentTable.slug + "/" + slug;
        }
        return slug;
    }

    public String html(ProjectsTable project) {
        // TODO markdown renderer
        return contents;
    }

    public boolean isHome() {
        return name.equals("Home") && parentId == null;
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


    public boolean getIsDeletable() {
        return isDeletable;
    }

    public void setIsDeletable(boolean isDeletable) {
        this.isDeletable = isDeletable;
    }


    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

}
