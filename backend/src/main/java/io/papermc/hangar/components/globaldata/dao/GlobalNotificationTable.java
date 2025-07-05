package io.papermc.hangar.components.globaldata.dao;

import io.papermc.hangar.model.db.Table;
import java.time.OffsetDateTime;
import java.util.Objects;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

public class GlobalNotificationTable extends Table {

    private String key;
    private String content;
    private String color;
    private OffsetDateTime activeFrom;
    private OffsetDateTime activeTo;
    private long createdBy;

    @JdbiConstructor
    public GlobalNotificationTable(final OffsetDateTime createdAt, final long id, final String key, final String content, final String color, final OffsetDateTime activeFrom, final OffsetDateTime activeTo, final long createdBy) {
        super(createdAt, id);
        this.key = key;
        this.content = content;
        this.color = color;
        this.activeFrom = activeFrom;
        this.activeTo = activeTo;
        this.createdBy = createdBy;
    }

    public String getKey() {
        return key;
    }

    public void setKey(final String key) {
        this.key = key;
    }

    public String getContent() {
        return content;
    }

    public void setContent(final String content) {
        this.content = content;
    }

    public String getColor() {
        return color;
    }

    public void setColor(final String color) {
        this.color = color;
    }

    public OffsetDateTime getActiveFrom() {
        return activeFrom;
    }

    public void setActiveFrom(final OffsetDateTime activeFrom) {
        this.activeFrom = activeFrom;
    }

    public OffsetDateTime getActiveTo() {
        return activeTo;
    }

    public void setActiveTo(final OffsetDateTime activeTo) {
        this.activeTo = activeTo;
    }

    public long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(final long createdBy) {
        this.createdBy = createdBy;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        GlobalNotificationTable that = (GlobalNotificationTable) o;
        return createdBy == that.createdBy && Objects.equals(key, that.key) && Objects.equals(content, that.content) && Objects.equals(color, that.color) && Objects.equals(activeFrom, that.activeFrom) && Objects.equals(activeTo, that.activeTo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), key, content, color, activeFrom, activeTo, createdBy);
    }

    @Override
    public String toString() {
        return "GlobalNotificationTable{" +
            "key='" + key + '\'' +
            ", content='" + content + '\'' +
            ", color='" + color + '\'' +
            ", activeFrom=" + activeFrom +
            ", activeTo=" + activeTo +
            ", createdBy=" + createdBy +
            "} " + super.toString();
    }
}
