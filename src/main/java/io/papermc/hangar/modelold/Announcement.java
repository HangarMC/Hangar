package io.papermc.hangar.modelold;

import java.util.Objects;
import java.util.StringJoiner;

public class Announcement {

    private String text;
    private String color;

    public Announcement() {
    }

    public Announcement(String text, String color) {
        this.text = text;
        this.color = color;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Announcement.class.getSimpleName() + "[", "]")
                .add("text='" + text + "'")
                .add("color='" + color + "'")
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Announcement that = (Announcement) o;
        return Objects.equals(text, that.text) && Objects.equals(color, that.color);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, color);
    }
}
