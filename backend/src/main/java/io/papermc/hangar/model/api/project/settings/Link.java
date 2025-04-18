package io.papermc.hangar.model.api.project.settings;

import io.papermc.hangar.controller.validations.Validate;
import jakarta.validation.constraints.NotNull;

// @el(root: String)
public record Link(long id, @Validate(SpEL = "@validate.max(#root, @'hangar-io.papermc.hangar.config.hangar.HangarConfig'.pages.maxNameLen())") String name, @Validate(SpEL = "@validate.regex(#root, @'hangar-io.papermc.hangar.config.hangar.HangarConfig'.urlRegex)", message = "fieldError.url") @NotNull String url) {
}
