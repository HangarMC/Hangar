package io.papermc.hangar.model;

import java.util.Objects;
import java.util.StringJoiner;

public class Announcement {

    private String text;
    private String color;

    public Announcement() {
    }

    public Announcement(final String text, final String color) {
        this.text = text;
        this.color = color;
    }

    public String getText() {
        return this.text;
    }

    public void setText(final String text) {
        this.text = text;
    }

    public String getColor() {
        return this.color;
    }

    public void setColor(final String color) {
        this.color = color;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Announcement.class.getSimpleName() + "[", "]")
            .add("text='" + this.text + "'")
            .add("color='" + this.color + "'")
            .toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        final Announcement that = (Announcement) o;
        return Objects.equals(this.text, that.text) && Objects.equals(this.color, that.color);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.text, this.color);
    }
}
