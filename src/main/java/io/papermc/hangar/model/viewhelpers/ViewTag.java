package io.papermc.hangar.model.viewhelpers;

import io.papermc.hangar.model.TagColor;
import io.papermc.hangar.db.model.ProjectVersionTagsTable;

public class ViewTag {

    private final String name;
    private final String data;
    private final TagColor color;

    public ViewTag(String name, String data, TagColor color) {
        this.name = name;
        this.data = data;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public String getData() {
        return data;
    }

    public TagColor getColor() {
        return color;
    }

    public static ViewTag fromVersionTag(ProjectVersionTagsTable versionTag) {
        return new ViewTag(versionTag.getName(), versionTag.getData(), versionTag.getColor());
    }
}
