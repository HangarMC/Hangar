package io.papermc.hangar.model.api.project.version;

import io.papermc.hangar.model.common.Platform;

import java.util.Map;

public record VersionStats(long totalDownloads, Map<Platform, Long> platformDownloads) {
}
