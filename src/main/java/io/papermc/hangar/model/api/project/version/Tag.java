package io.papermc.hangar.model.api.project.version;

import io.papermc.hangar.model.Named;
import io.papermc.hangar.model.api.color.TagColor;

import java.util.Objects;

public class Tag implements Named {

    private final String name;
    private final String data;
    private final TagColor color;

    public Tag(String name, String data, TagColor color) {
        this.name = name;
        this.data = data;
        this.color = color;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getData() {
        return data;
    }

    public TagColor getColor() {
        return color;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tag tag = (Tag) o;
        return name.equals(tag.name) && data.equals(tag.data) && color.equals(tag.color);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, data, color);
    }

    @Override
    public String toString() {
        return "Tag{" +
                "name='" + name + '\'' +
                ", data='" + data + '\'' +
                ", color=" + color +
                '}';
    }
}
