package io.papermc.hangar.model.api.project.version;

import org.jetbrains.annotations.Nullable;

public record PlatformVersionDownload(@Nullable FileInfo fileInfo, @Nullable String externalUrl) {
}
