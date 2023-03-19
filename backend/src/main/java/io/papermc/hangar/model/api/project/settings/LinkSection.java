package io.papermc.hangar.model.api.project.settings;

import io.papermc.hangar.controller.validations.Validate;
import java.util.List;

public record LinkSection(long id, String type, @Validate(SpEL = "@validate.max(#root, @hangarConfig.pages.maxNameLen())") String title, List<Link> links) {
}
