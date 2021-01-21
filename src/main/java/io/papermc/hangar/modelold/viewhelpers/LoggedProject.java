package io.papermc.hangar.modelold.viewhelpers;

import org.jetbrains.annotations.Nullable;

public class LoggedProject {

    private final Long id;
    private final String slug;
    private final String owner;

    public LoggedProject(@Nullable Long id, @Nullable String slug, @Nullable String owner) {
        this.id = id;
        this.slug = slug;
        this.owner = owner;
    }

    public Long getId() {
        return id;
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
                ", slug='" + slug + '\'' +
                ", owner='" + owner + '\'' +
                '}';
    }
}
