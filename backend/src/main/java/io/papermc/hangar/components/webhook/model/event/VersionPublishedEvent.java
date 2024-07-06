package io.papermc.hangar.components.webhook.model.event;

import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

public class VersionPublishedEvent extends ProjectEvent {

    public static final String TYPE = "version_published";

    private final String version;
    private final String description;

    public VersionPublishedEvent(final String author, final String name, final String avatar, final String url, final String version, final String description, final List<String> platforms) {
        super(TYPE, author, name, avatar, url, platforms);
        this.description = description;
        this.version = version;
    }

    public String getVersion() {
        return this.version;
    }

    public String getDescription() {
        return this.description;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        final VersionPublishedEvent that = (VersionPublishedEvent) o;
        return Objects.equals(this.version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.version);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", VersionPublishedEvent.class.getSimpleName() + "[", "]")
            .add("version='" + this.version + "'")
            .add("description='" + this.description + "'")
            .toString();
    }
}
