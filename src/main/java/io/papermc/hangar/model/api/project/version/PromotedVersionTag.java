package io.papermc.hangar.model.api.project.version;

import io.papermc.hangar.model.TagColor;

public class PromotedVersionTag extends Tag {

    private final String displayData;
    private final String minecraftVersion;

    public PromotedVersionTag(String name, String data, String displayData, String minecraftVersion, TagColor color) {
        super(name, data, color.toTagColor());
        this.displayData = displayData;
        this.minecraftVersion = minecraftVersion;
    }

    public String getDisplayData() {
        return displayData;
    }

    public String getMinecraftVersion() {
        return minecraftVersion;
    }

    @Override
    public String toString() {
        return "PromotedVersionTag{" +
                "displayData='" + displayData + '\'' +
                ", minecraftVersion='" + minecraftVersion + '\'' +
                "} " + super.toString();
    }
}
