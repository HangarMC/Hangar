package io.papermc.hangar.model.internal.logs.viewmodels;

import org.jdbi.v3.core.mapper.PropagateNull;

public class LogPage {

    private final Long id;
    private final String name;
    private final String slug;

    public LogPage(@PropagateNull final Long id, final String name, final String slug) {
        this.id = id;
        this.name = name;
        this.slug = slug;
    }

    public Long getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getSlug() {
        return this.slug;
    }

    @Override
    public String toString() {
        return "LogPage{" +
            "id=" + this.id +
            ", name='" + this.name + '\'' +
            ", slug='" + this.slug + '\'' +
            '}';
    }
}
