package io.papermc.hangar.model.api.project;

import java.util.List;

public class PromotedVersion {

    private final String version;
    private final List<PromotedVersionTag> tags;

    public PromotedVersion(String version, List<PromotedVersionTag> tags) {
        this.version = version;
        this.tags = tags;
    }

    public String getVersion() {
        return version;
    }

    public List<PromotedVersionTag> getTags() {
        return tags;
    }
}
