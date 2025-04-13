package io.papermc.hangar.model.api.project;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.papermc.hangar.config.hangar.HangarConfig;
import io.papermc.hangar.controller.validations.Validate;
import io.papermc.hangar.util.StaticContextAccessor;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * @param name @el(root: String)
 * @param url  @el(root: String)
 * @param type @el(root: String)
 */
public record ProjectLicense(@Validate(SpEL = "@validations.regex(#root, @hangarConfig.projects.licenseNameRegex)", message = "project.new.error.invalidLicense")
                             @Validate(SpEL = "@validations.max(#root, @hangarConfig.projects.maxLicenseNameLen)", message = "project.new.error.tooLongLicense")
                             String name,
                             @Validate(SpEL = "@validate.regex(#root, @hangarConfig.urlRegex)", message = "fieldError.url")
                             String url,
                             @Validate(SpEL = "@validate.required(#root)")
                             @Validate(SpEL = "@validations.regex(#root, @hangarConfig.projects.licenseNameRegex)", message = "project.new.error.invalidLicense")
                             @Validate(SpEL = "@validations.max(#root, @hangarConfig.projects.maxLicenseNameLen)", message = "project.new.error.tooLongLicense")
                             String type) {

    private static final HangarConfig config = StaticContextAccessor.getBean(HangarConfig.class);

    @JsonCreator
    @JdbiConstructor
    public ProjectLicense(final @Nullable String name, final @Nullable String url, final @Nullable String type) {
        this.name = name;
        this.url = url;
        if (config.getLicenses().contains(type)) {
            this.type = type;
        } else {
            this.type = "(custom)";
        }
    }
}
