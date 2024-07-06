package io.papermc.hangar.components.webhook.model.event;

import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

public abstract class ProjectEvent extends WebhookEvent {

    private final String author;
    private final String name;
    private final String avatar;
    private final String url;
    private final List<String> platforms;

    protected ProjectEvent(final String type, final String author, final String name, final String avatar, final String url, final List<String> platforms) {
        super(type);
        this.author = author;
        this.name = name;
        this.avatar = avatar;
        this.url = url;
        this.platforms = platforms;
    }

    public String getAuthor() {
        return this.author;
    }

    public String getName() {
        return this.name;
    }

    public String getAvatar() {
        return this.avatar;
    }

    public String getUrl() {
        return this.url;
    }

    public List<String> getPlatforms() {
        return this.platforms;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        final ProjectEvent that = (ProjectEvent) o;
        return Objects.equals(this.author, that.author) && Objects.equals(this.name, that.name) && Objects.equals(this.avatar, that.avatar) && Objects.equals(this.url, that.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.author, this.name, this.avatar, this.url);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ProjectEvent.class.getSimpleName() + "[", "]")
            .add("author='" + this.author + "'")
            .add("name='" + this.name + "'")
            .add("avatar='" + this.avatar + "'")
            .add("url='" + this.url + "'")
            .add("platforms='" + this.platforms + "'")
            .toString();
    }
}
