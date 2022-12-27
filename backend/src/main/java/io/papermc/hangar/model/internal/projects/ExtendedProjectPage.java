package io.papermc.hangar.model.internal.projects;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.papermc.hangar.model.db.projects.ProjectPageTable;
import java.time.OffsetDateTime;

public class ExtendedProjectPage extends ProjectPageTable {

    private final boolean home;

    public ExtendedProjectPage(final OffsetDateTime createdAt, final long id, final long projectId, final String name, final String slug, final String contents, final boolean deletable, final Long parentId, final boolean home) {
        super(createdAt, id, projectId, name, slug, contents, deletable, parentId);
        this.home = home;
    }

    @JsonProperty("isHome")
    public boolean isHome() {
        return this.home;
    }

    @Override
    public String toString() {
        return "HangarViewProjectPage{" +
            "home=" + this.home +
            "} " + super.toString();
    }
}
