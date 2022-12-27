package io.papermc.hangar.model.internal.logs.viewmodels;

import org.jdbi.v3.core.mapper.PropagateNull;
import org.jdbi.v3.core.mapper.reflect.ColumnName;

public class LogProject {

    private final Long id;
    private final String slug;
    private final String owner;

    public LogProject(@PropagateNull final Long id, final String slug, @ColumnName("owner_name") final String owner) {
        this.id = id;
        this.slug = slug;
        this.owner = owner;
    }

    public Long getId() {
        return this.id;
    }

    public String getSlug() {
        return this.slug;
    }

    public String getOwner() {
        return this.owner;
    }

    @Override
    public String toString() {
        return "LogProject{" +
                "id=" + this.id +
                ", slug='" + this.slug + '\'' +
                ", owner='" + this.owner + '\'' +
                '}';
    }
}
