package io.papermc.hangar.components.globaldata.dao;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.papermc.hangar.model.db.Table;
import java.time.OffsetDateTime;
import java.util.Objects;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

public class AnnouncementTable extends Table {

    private String text;
    private String color;
    private long createdBy;

    @JsonCreator
    @JdbiConstructor
    public AnnouncementTable(final long id, final OffsetDateTime createdAt, final String text, final String color, final long createdBy) {
        super(createdAt, id);
        this.text = text;
        this.color = color;
        this.createdBy = createdBy;
    }

    public AnnouncementTable(final String text, final String color, final  long createdBy) {
        this.text = text;
        this.color = color;
        this.createdBy = createdBy;
    }

    public String getText() {
        return text;
    }

    public void setText(final String text) {
        this.text = text;
    }

    public String getColor() {
        return color;
    }

    public void setColor(final String color) {
        this.color = color;
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
        AnnouncementTable that = (AnnouncementTable) o;
        return createdBy == that.createdBy && Objects.equals(text, that.text) && Objects.equals(color, that.color);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), text, color, createdBy);
    }

    @Override
    public String toString() {
        return "AnnouncementTable{" +
            "text='" + text + '\'' +
            ", color='" + color + '\'' +
            ", createdBy=" + createdBy +
            "} " + super.toString();
    }
}
