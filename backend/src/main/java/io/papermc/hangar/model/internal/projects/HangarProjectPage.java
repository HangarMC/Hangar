package io.papermc.hangar.model.internal.projects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.papermc.hangar.model.db.projects.ProjectPageTable;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class HangarProjectPage {

    private final long id;
    private final String name;
    private final String slug;
    private final boolean home;
    private final Map<Long, HangarProjectPage> children;

    public HangarProjectPage(final ProjectPageTable projectPageTable, final boolean home) {
        this.id = projectPageTable.getId();
        this.name = projectPageTable.getName();
        this.slug = projectPageTable.getSlug();
        this.home = home;
        this.children = new LinkedHashMap<>();
    }

    public long getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getSlug() {
        return this.slug;
    }

    public boolean isHome() {
        return this.home;
    }

    @JsonIgnore
    public Map<Long, HangarProjectPage> getChildren() {
        return this.children;
    }

    @JsonProperty("children")
    public Collection<HangarProjectPage> getPageChildren() {
        return this.children.values();
    }

    @Override
    public String toString() {
        return "HangarProjectPage{" +
            "name='" + this.name + '\'' +
            ", slug='" + this.slug + '\'' +
            ", children=" + this.children +
            '}';
    }
}
