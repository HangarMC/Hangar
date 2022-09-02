package io.papermc.hangar.model.internal.logs.viewmodels;

import org.jdbi.v3.core.mapper.PropagateNull;
import org.jdbi.v3.core.mapper.reflect.ColumnName;

public class LogProject {

    private final Long id;
    private final String slug;
    private final String owner;

    public LogProject(@PropagateNull Long id, String slug, @ColumnName("owner_name") String owner) {
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
        return "LogProject{" +
                "id=" + id +
                ", slug='" + slug + '\'' +
                ", owner='" + owner + '\'' +
                '}';
    }
}
