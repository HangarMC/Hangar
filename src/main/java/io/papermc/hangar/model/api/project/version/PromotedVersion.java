package io.papermc.hangar.model.api.project.version;

import java.util.List;

@Deprecated(forRemoval = true)
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

    @Override
    public String toString() {
        return "PromotedVersion{" +
                "version='" + version + '\'' +
                ", tags=" + tags +
                '}';
    }
}
