package io.papermc.hangar.model.api.project.settings;

import io.papermc.hangar.controller.validations.Validate;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record LinkSection(long id,
                          @NotNull String type,
                          @Validate(SpEL = "@validate.max(#root, @hangarConfig.pages.maxNameLen())") String title,
                          @NotNull @Validate(SpEL = "@validate.max(#root, 10)", message = "Too many links") List<@Valid Link> links) {
}
