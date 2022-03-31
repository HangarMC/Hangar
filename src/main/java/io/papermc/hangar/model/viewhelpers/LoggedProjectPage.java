package io.papermc.hangar.model.viewhelpers;

import org.jetbrains.annotations.Nullable;

public class LoggedProjectPage {

    private final Long id;
    private final String name;
    private final String slug;

    public LoggedProjectPage(@Nullable Long id, @Nullable String name, @Nullable String slug) {
        this.id = id;
        this.name = name;
        this.slug = slug;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSlug() {
        return slug;
    }

    @Override
    public String toString() {
        return "LoggedProjectPage{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", slug='" + slug + '\'' +
                '}';
    }
}
