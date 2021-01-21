package io.papermc.hangar.modelold.viewhelpers;

import io.papermc.hangar.db.modelold.ProjectVersionTagsTable;
import io.papermc.hangar.modelold.TagColor;

import java.util.List;

public class ViewTag {

    private final String name;
    private final List<String> data;
    private final TagColor color;

    public ViewTag(String name, List<String> data, TagColor color) {
        this.name = name;
        this.data = data;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public List<String> getData() {
        return data;
    }

    public TagColor getColor() {
        return color;
    }

    public static ViewTag fromVersionTag(ProjectVersionTagsTable versionTag) {
        return new ViewTag(versionTag.getName(), versionTag.getData(), versionTag.getColor());
    }
}
