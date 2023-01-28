package io.papermc.hangar.model.api.project.version;

import io.papermc.hangar.model.common.Platform;

import java.util.Map;

public class VersionStats {

    private final long totalDownloads;
    private final Map<Platform, Long> platformDownloads;

    public VersionStats(final long totalDownloads, final Map<Platform, Long> platformDownloads) {
        this.totalDownloads = totalDownloads;
        this.platformDownloads = platformDownloads;
    }

    public long getTotalDownloads() {
        return this.totalDownloads;
    }

    public Map<Platform, Long> getPlatformDownloads() {
        return this.platformDownloads;
    }

    @Override
    public String toString() {
        return "VersionStats{" +
            "totalDownloads=" + this.totalDownloads +
            "platformDownloads=" + this.platformDownloads +
            '}';
    }
}
