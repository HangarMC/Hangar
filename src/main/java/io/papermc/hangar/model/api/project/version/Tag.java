package io.papermc.hangar.model.api.project.version;

import io.papermc.hangar.model.Named;
import io.papermc.hangar.model.api.color.TagColor;

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
    public String toString() {
        return "Tag{" +
                "name='" + name + '\'' +
                ", data='" + data + '\'' +
                ", color=" + color +
                '}';
    }
}
