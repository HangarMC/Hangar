package io.papermc.hangar.model.viewhelpers;

import io.papermc.hangar.db.model.ProjectPagesTable;

public class ProjectPage extends ProjectPagesTable {

    public ProjectPage(long projectId, String name, String slug, String contents, boolean isDeletable, Long parentId) {
        super(projectId, name, slug, contents, isDeletable, parentId);
    }

    public ProjectPage() { }

    public boolean isHome() {
        return this.getName().equals("Home") && this.getParentId() == null; // TODO check against config default
    }
}
