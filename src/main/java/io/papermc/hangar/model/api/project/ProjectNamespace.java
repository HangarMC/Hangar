package io.papermc.hangar.model.api.project;

import org.jdbi.v3.core.mapper.PropagateNull;

import java.util.Objects;

public class ProjectNamespace {

    private final String owner;
    private final String slug;

    public ProjectNamespace(@PropagateNull String owner, String slug) {
        this.owner = owner;
        this.slug = slug;
    }

    public String getOwner() {
        return owner;
    }

    public String getSlug() {
        return slug;
    }

    @Override
    public String toString() {
        return "ProjectNamespace{" +
                "owner='" + owner + '\'' +
                ", slug='" + slug + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProjectNamespace that = (ProjectNamespace) o;
        return owner.equals(that.owner) && slug.equals(that.slug);
    }

    @Override
    public int hashCode() {
        return Objects.hash(owner, slug);
    }
}

