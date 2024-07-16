package io.papermc.hangar.components.webhook.model;

import io.papermc.hangar.model.db.Table;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.StringJoiner;

public class WebhookTable extends Table {

    private String name;
    private String url;
    private String secret;
    private boolean active;
    // TODO rename type to template and link it to a template table
    private String type;
    private String[] events;
    private String scope;

    public WebhookTable(final OffsetDateTime createdAt, final long id, final String name, final String url, final String secret, final boolean active, final String type, final String[] events, final String scope) {
        super(createdAt, id);
        this.name = name;
        this.url = url;
        this.secret = secret;
        this.active = active;
        this.type = type;
        this.scope = scope;
    }

    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public String getSecret() {
        return this.secret;
    }

    public void setSecret(final String secret) {
        this.secret = secret;
    }

    public boolean isActive() {
        return this.active;
    }

    public void setActive(final boolean active) {
        this.active = active;
    }

    public String getType() {
        return this.type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public String[] getEvents() {
        return this.events;
    }

    public void setEvents(final String[] events) {
        this.events = events;
    }

    public String getScope() {
        return this.scope;
    }

    public void setScope(final String scope) {
        this.scope = scope;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        final WebhookTable that = (WebhookTable) o;
        return this.active == that.active && Objects.equals(this.name, that.name) && Objects.equals(this.url, that.url) && Objects.equals(this.secret, that.secret) && Objects.equals(this.type, that.type) && Objects.equals(this.scope, that.scope);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.name, this.url, this.secret, this.active, this.type, this.scope);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", WebhookTable.class.getSimpleName() + "[", "]")
            .add("name='" + this.name + "'")
            .add("url='" + this.url + "'")
            .add("secret='" + this.secret + "'")
            .add("active=" + this.active)
            .add("type='" + this.type + "'")
            .add("scope='" + this.scope + "'")
            .add("createdAt=" + this.createdAt)
            .add("id=" + this.id)
            .toString();
    }
}
