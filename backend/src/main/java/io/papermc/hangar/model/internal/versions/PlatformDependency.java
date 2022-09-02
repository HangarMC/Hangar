package io.papermc.hangar.model.internal.versions;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.papermc.hangar.model.common.Platform;

import java.util.List;

public class PlatformDependency {

    private final Platform platform;
    private final List<String> versions;

    @JsonCreator
    public PlatformDependency(Platform platform, List<String> versions) {
        this.platform = platform;
        this.versions = versions;
    }

    public Platform getPlatform() {
        return platform;
    }

    public List<String> getVersions() {
        return versions;
    }

    @Override
    public String toString() {
        return "PlatformDependency{" +
                "platform=" + platform +
                ", versions=" + versions +
                '}';
    }
}
