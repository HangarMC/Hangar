package io.papermc.hangar.model.internal.api.requests.versions;

import io.papermc.hangar.model.common.Platform;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.SortedSet;

public record UpdatePlatformVersions(@NotNull Platform platform,
                                     @Size(min = 1, message = "version.edit.error.noPlatformVersions") SortedSet<@NotBlank(message = "version.new.error.invalidPlatformVersion") String> versions) {
}
