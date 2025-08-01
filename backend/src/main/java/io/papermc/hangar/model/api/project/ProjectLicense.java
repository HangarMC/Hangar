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
public record ProjectLicense(@Validate(SpEL = "@validations.regex(#root, @'hangar-io.papermc.hangar.config.hangar.HangarConfig'.projects.licenseNameRegex)", message = "project.new.error.invalidLicense")
                             @Validate(SpEL = "@validations.max(#root, @'hangar-io.papermc.hangar.config.hangar.HangarConfig'.projects.maxLicenseNameLen)", message = "project.new.error.tooLongLicense")
                             String name,
                             @Validate(SpEL = "@validate.regex(#root, @'hangar-io.papermc.hangar.config.hangar.HangarConfig'.urlRegex)", message = "fieldError.url")
                             @Nullable
                             String url,
                             @Validate(SpEL = "@validate.required(#root)")
                             @Validate(SpEL = "@validations.regex(#root, @'hangar-io.papermc.hangar.config.hangar.HangarConfig'.projects.licenseNameRegex)", message = "project.new.error.invalidLicense")
                             @Validate(SpEL = "@validations.max(#root, @'hangar-io.papermc.hangar.config.hangar.HangarConfig'.projects.maxLicenseNameLen)", message = "project.new.error.tooLongLicense")
                             String type) {

    private static final HangarConfig config = StaticContextAccessor.getBean(HangarConfig.class);

    @JsonCreator
    @JdbiConstructor
    public ProjectLicense(final @Nullable String name, final @Nullable String url, final @Nullable String type) {
        this.name = name;
        this.url = url;
        if (config.licenses().contains(type)) {
            this.type = type;
        } else {
            this.type = "(custom)";
        }
    }
}
