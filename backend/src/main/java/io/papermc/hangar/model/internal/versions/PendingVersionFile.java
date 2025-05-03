package io.papermc.hangar.model.internal.versions;

import io.papermc.hangar.controller.validations.Validate;
import io.papermc.hangar.model.api.project.version.FileInfo;
import io.papermc.hangar.model.common.Platform;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.checkerframework.checker.nullness.qual.Nullable;

// @el(root: String) // can't figure out how to apply this to just the field
public record PendingVersionFile(
    @Size(min = 1, max = 3, message = "version.new.error.invalidNumOfPlatforms") List<Platform> platforms,
    @Nullable FileInfo fileInfo,
    @Validate(SpEL = "@validate.regex(#root, @'hangar-io.papermc.hangar.config.hangar.HangarConfig'.urlRegex)", message = "fieldError.url") @Nullable String externalUrl) {
}
