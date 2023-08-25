package io.papermc.hangar.model.api.project;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Objects;
import org.jdbi.v3.core.mapper.PropagateNull;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

public class ProjectNamespace {

    private final String owner;
    @Schema(description = "The unique name of a project", example = "Maintenance")
    private final String slug;

    @JdbiConstructor
    public ProjectNamespace(@PropagateNull final String owner, final String slug) {
        this.owner = owner;
        this.slug = slug;
    }

    public ProjectNamespace(final String namespace) {
        final String[] split = namespace.split("/");
        if (split.length != 2) {
            throw new IllegalArgumentException("Namespace must be <owner>/<slug> but was '" + namespace + "'");
        }
        this.owner = split[0];
        this.slug = split[1];
    }

    public String getOwner() {
        return this.owner;
    }

    public String getSlug() {
        return this.slug;
    }

    @Override
    public String toString() {
        return this.owner + "/" + this.slug;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        final ProjectNamespace that = (ProjectNamespace) o;
        return this.owner.equals(that.owner) && this.slug.equals(that.slug);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.owner, this.slug);
    }
}

