package io.papermc.hangar.model.api.project.settings;

import io.papermc.hangar.controller.validations.Validate;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record LinkSection(long id,
                          @NotNull @Schema(description = "Type of the link. Either SIDEBAR or TOP", example = "TOP") String type,
                          @Validate(SpEL = "@validate.max(#root, @hangarConfig.pages.maxNameLen())") String title,
                          @NotNull @Validate(SpEL = "@validate.max(#root, 10)", message = "Too many links") List<@Valid Link> links) {
}
