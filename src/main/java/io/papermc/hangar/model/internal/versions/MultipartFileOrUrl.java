package io.papermc.hangar.model.internal.versions;

import io.papermc.hangar.controller.validations.Validate;
import io.papermc.hangar.model.common.Platform;
import org.jetbrains.annotations.Nullable;

import javax.validation.constraints.Size;
import java.util.List;

public record MultipartFileOrUrl(
    @Size(min = 1, max = 3, message = "version.new.error.invalidNumOfPlatforms") List<Platform> platforms,
    @Nullable @Validate(SpEL = "@validate.regex(#root, @hangarConfig.urlRegex)", message = "validation.invalidUrl") String externalUrl) {

    public boolean isUrl() {
        return externalUrl != null && !externalUrl.isEmpty();
    }
}
