package io.papermc.hangar.model.api.project;

import io.papermc.hangar.modelold.TagColor;

public class PromotedVersionTag {

    private final String name;
    private final String data;
    private final String displayData;
    private final String minecraftVersion;
    private final TagColor color;

    public PromotedVersionTag(String name, String data, String displayData, String minecraftVersion, TagColor color) {
        this.name = name;
        this.data = data;
        this.displayData = displayData;
        this.minecraftVersion = minecraftVersion;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public String getData() {
        return data;
    }

    public String getDisplayData() {
        return displayData;
    }

    public String getMinecraftVersion() {
        return minecraftVersion;
    }

    public TagColor getColor() {
        return color;
    }
}
