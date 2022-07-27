package io.papermc.hangar.model.internal.versions;

import io.papermc.hangar.controller.validations.Validate;
import io.papermc.hangar.model.api.project.version.FileInfo;
import io.papermc.hangar.model.common.Platform;
import org.jetbrains.annotations.Nullable;

import javax.validation.constraints.Size;
import java.util.List;

public record PendingVersionFile(
    @Size(min = 1, max = 3, message = "version.new.error.invalidNumOfPlatforms") List<Platform> platforms,
    @Nullable FileInfo fileInfo,
    @Validate(SpEL = "@validate.regex(#root, @hangarConfig.urlRegex)", message = "validation.invalidUrl") @Nullable String externalUrl) {
}
