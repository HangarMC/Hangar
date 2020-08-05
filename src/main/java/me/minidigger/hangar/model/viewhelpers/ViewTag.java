package me.minidigger.hangar.model.viewhelpers;

import me.minidigger.hangar.db.model.ProjectVersionTagsTable;
import me.minidigger.hangar.model.TagColor;

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
