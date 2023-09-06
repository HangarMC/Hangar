package io.papermc.hangar.model.internal.versions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.papermc.hangar.controller.validations.Validate;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.common.Platform;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.jetbrains.annotations.Nullable;

// @el(root: String) // can't figure out how to apply this to just the one record component
@Schema(description = "List of different jars/external links that are part of the version")
public record MultipartFileOrUrl(
    @Schema(description = "List of platforms this jar runs on", example = "[PAPER, WATERFALL, VELOCITY]") List<Platform> platforms,
    @Nullable @Validate(SpEL = "@validate.regex(#root, @hangarConfig.urlRegex)", message = "validation.invalidUrl")
    @Schema(description = "External url to download the jar from if not provided via an attached jar, else null", example = "https://papermc.io/downloads")
    String externalUrl
) {

    public MultipartFileOrUrl {
        if (platforms == null || platforms.isEmpty() || platforms.size() > 3) {
            throw new HangarApiException("version.new.error.invalidNumOfPlatforms");
        }
    }

    @JsonIgnore
    public boolean isUrl() {
        return this.externalUrl != null && !this.externalUrl.isEmpty();
    }
}
