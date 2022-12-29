package io.papermc.hangar.model.api.project;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.papermc.hangar.config.hangar.HangarConfig;
import io.papermc.hangar.controller.validations.Validate;
import io.papermc.hangar.util.StaticContextAccessor;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;
import org.jetbrains.annotations.Nullable;

public class ProjectLicense {

    private static final HangarConfig config = StaticContextAccessor.getBean(HangarConfig.class);

    // @el(root: String)
    private final @Validate(SpEL = "@validations.regex(#root, @hangarConfig.projects.licenseNameRegex)", message = "project.new.error.invalidLicense")
    @Validate(SpEL = "@validations.max(#root, @hangarConfig.projects.maxLicenseNameLen)", message = "project.new.error.tooLongLicense") String name;

    // @el(root: String)
    private final @Validate(SpEL = "@validate.regex(#root, @hangarConfig.urlRegex)", message = "validation.invalidUrl") String url;

    // @el(root: String)
    private final @Validate(SpEL = "@validate.required(#root)")
    @Validate(SpEL = "@validations.regex(#root, @hangarConfig.projects.licenseNameRegex)", message = "project.new.error.invalidLicense")
    @Validate(SpEL = "@validations.max(#root, @hangarConfig.projects.maxLicenseNameLen)", message = "project.new.error.tooLongLicense") String type;

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

    public String getName() {
        return this.name;
    }

    public @Nullable String getUrl() {
        return this.url;
    }

    public String getType() {
        return this.type;
    }

    @Override
    public String toString() {
        return "ProjectLicense{" +
            "name='" + this.name + '\'' +
            ", url='" + this.url + '\'' +
            ", type='" + this.type + '\'' +
            '}';
    }
}
