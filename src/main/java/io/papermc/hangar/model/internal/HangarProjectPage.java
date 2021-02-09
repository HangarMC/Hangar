package io.papermc.hangar.model.internal;

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
    private final Map<Long, HangarProjectPage> children;

    public HangarProjectPage(ProjectPageTable projectPageTable) {
        this.id = projectPageTable.getId();
        this.name = projectPageTable.getName();
        this.slug = projectPageTable.getSlug();
        this.children = new LinkedHashMap<>();
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSlug() {
        return slug;
    }

    @JsonIgnore
    public Map<Long, HangarProjectPage> getChildren() {
        return children;
    }

    @JsonProperty("children")
    public Collection<HangarProjectPage> getPageChildren() {
        return children.values();
    }

    @Override
    public String toString() {
        return "HangarProjectPage{" +
                "name='" + name + '\'' +
                ", slug='" + slug + '\'' +
                ", children=" + children +
                '}';
    }
}
