package io.papermc.hangar.model.internal.versions;

import io.papermc.hangar.controller.validations.Validate;
import io.papermc.hangar.model.common.Platform;
import java.util.List;
import javax.validation.constraints.Size;
import org.jetbrains.annotations.Nullable;

// @el(root: String) // can't figure out how to apply this to just the one record component
public record MultipartFileOrUrl(
    @Size(min = 1, max = 3, message = "version.new.error.invalidNumOfPlatforms") List<Platform> platforms,
    @Nullable @Validate(SpEL = "@validate.regex(#root, @hangarConfig.urlRegex)", message = "validation.invalidUrl") String externalUrl) {

    public boolean isUrl() {
        return this.externalUrl != null && !this.externalUrl.isEmpty();
    }
}
