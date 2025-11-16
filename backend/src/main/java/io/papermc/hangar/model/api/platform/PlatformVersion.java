package io.papermc.hangar.model.api.platform;

import java.util.SequencedSet;

public record PlatformVersion(String version, SequencedSet<String> subVersions) {

    public boolean contains(final String version) {
        return this.subVersions.contains(version);
    }
}
