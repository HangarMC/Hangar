package io.papermc.hangar.model.internal;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.papermc.hangar.model.db.projects.ProjectPageTable;

import java.time.OffsetDateTime;

// TODO rename? Different from HangarProjectPage as it just represents one page but needs to include the isHome field
public class HangarViewProjectPage extends ProjectPageTable {

    private final boolean home;

    public HangarViewProjectPage(OffsetDateTime createdAt, long id, long projectId, String name, String slug, String contents, boolean deletable, Long parentId, boolean home) {
        super(createdAt, id, projectId, name, slug, contents, deletable, parentId);
        this.home = home;
    }

    @JsonProperty("isHome")
    public boolean isHome() {
        return home;
    }

    @Override
    public String toString() {
        return "HangarViewProjectPage{" +
                "home=" + home +
                "} " + super.toString();
    }
}
