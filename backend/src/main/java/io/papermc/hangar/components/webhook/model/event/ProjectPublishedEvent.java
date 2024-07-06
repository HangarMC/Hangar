package io.papermc.hangar.components.webhook.model.event;

import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

public class ProjectPublishedEvent extends ProjectEvent {

    public static final String TYPE = "project_published";

    private final String description;

    public ProjectPublishedEvent(final String author, final String name, final String avatar, final String url, final String description, final List<String> platforms) {
        super(TYPE, author, name, avatar, url, platforms);
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        final ProjectPublishedEvent that = (ProjectPublishedEvent) o;
        return Objects.equals(this.description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.description);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ProjectPublishedEvent.class.getSimpleName() + "[", "]")
            .add("description='" + this.description + "'")
            .toString();
    }
}
