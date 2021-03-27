package io.papermc.hangar.model.api.project.version;

import io.papermc.hangar.model.common.TagColor;

import java.util.List;

public class PromotedVersionTag extends Tag {

    private final String displayData;
    private final List<String> minecraftVersions;

    public PromotedVersionTag(String name, String data, String displayData, List<String> minecraftVersions, TagColor color) {
        super(name, data, color.toTagColor());
        this.displayData = displayData;
        this.minecraftVersions = minecraftVersions;
    }

    public String getDisplayData() {
        return displayData;
    }

    public List<String> getMinecraftVersions() {
        return minecraftVersions;
    }

    @Override
    public String toString() {
        return "PromotedVersionTag{" +
                "displayData='" + displayData + '\'' +
                ", minecraftVersions=" + minecraftVersions +
                "} " + super.toString();
    }
}
