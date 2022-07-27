package io.papermc.hangar.model.internal.versions;

import io.papermc.hangar.controller.validations.Validate;
import io.papermc.hangar.model.common.Platform;
import java.util.List;
import javax.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

public record MultipartFileOrUrl(
    @Size(min = 1, max = 3, message = "version.new.error.invalidNumOfPlatforms") List<Platform> platforms,
    MultipartFile file,
    @Validate(SpEL = "@validate.regex(#root, @hangarConfig.urlRegex)", message = "validation.invalidUrl") String externalUrl) {

    public boolean isFile() {
        return externalUrl == null || !externalUrl.isEmpty();
    }
}
