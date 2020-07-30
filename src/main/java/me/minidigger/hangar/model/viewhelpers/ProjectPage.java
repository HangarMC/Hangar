package me.minidigger.hangar.model.viewhelpers;

import me.minidigger.hangar.db.model.ProjectPagesTable;
import org.jdbi.v3.core.annotation.Unmappable;
import org.springframework.lang.Nullable;

import java.time.OffsetDateTime;

public class ProjectPage {

    private ProjectPagesTable table;

    private long id;
    private OffsetDateTime createdAt;
    private String name;
    private String slug;
    private String contents;
    private boolean isDeletable;
    @Nullable
    private Long parentId;

    public ProjectPage(long id, OffsetDateTime createdAt, String name, String slug, String contents, boolean isDeletable, @Nullable Long parentId, ProjectPagesTable table) {
        this.createdAt = createdAt;
        this.name = name;
        this.slug = slug;
        this.contents = contents;
        this.isDeletable = isDeletable;
        this.parentId = parentId;
        this.table = table;
    }

    public ProjectPage() {
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
        return isDeletable;
    }

    public void setDeletable(boolean deletable) {
        isDeletable = deletable;
    }

    @Nullable
    public Long getParentId() {
        return parentId;
    }

    public void setParentId(@Nullable Long parentId) {
        this.parentId = parentId;
    }

    public boolean isHome() {
        return name.equals("Home") && parentId == null; // TODO check against config default
    }

//    public String getFullSlug(ProjectPage parent) {
//        if (parent != null) {
//            return parent.table.getSlug() + "/" + slug;
//        }
//        return slug;
//    }

    public static ProjectPage of(ProjectPagesTable pagesTable) {
        if (pagesTable == null) return null;
        return new ProjectPage(pagesTable.getId(), pagesTable.getCreatedAt(), pagesTable.getName(), pagesTable.getSlug(), pagesTable.getContents(), pagesTable.getIsDeletable(), pagesTable.getParentId(), pagesTable);
    }
}
