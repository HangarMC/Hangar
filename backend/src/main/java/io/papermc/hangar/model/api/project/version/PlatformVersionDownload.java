package io.papermc.hangar.model.api.project.version;

import io.swagger.v3.oas.annotations.media.Schema;
import org.jetbrains.annotations.Nullable;

public record PlatformVersionDownload(
    @Nullable FileInfo fileInfo,
    @Nullable @Schema(description = "External download url if not directly uploaded to Hangar") String externalUrl,
    @Nullable @Schema(description = "Hangar download url if not an external download") String downloadUrl
) {
}
