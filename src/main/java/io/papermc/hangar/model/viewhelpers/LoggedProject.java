package io.papermc.hangar.model.viewhelpers;

import org.jetbrains.annotations.Nullable;

public class LoggedProject {

    private final Long id;
    private final String pluginId;
    private final String slug;
    private final String owner;

    public LoggedProject(@Nullable Long id, @Nullable String pluginId, @Nullable String slug, @Nullable String owner) {
        this.id = id;
        this.pluginId = pluginId;
        this.slug = slug;
        this.owner = owner;
    }

    public Long getId() {
        return id;
    }

    public String getPluginId() {
        return pluginId;
    }

    public String getSlug() {
        return slug;
    }

    public String getOwner() {
        return owner;
    }

    @Override
    public String toString() {
        return "LoggedProject{" +
                "id=" + id +
                ", pluginId='" + pluginId + '\'' +
                ", slug='" + slug + '\'' +
                ", owner='" + owner + '\'' +
                '}';
    }
}
