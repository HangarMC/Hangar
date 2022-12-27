package io.papermc.hangar.model.api.project;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.papermc.hangar.config.hangar.HangarConfig;
import io.papermc.hangar.controller.validations.Validate;
import io.papermc.hangar.util.StaticContextAccessor;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;
import org.jetbrains.annotations.Nullable;

public class ProjectLicense {

    private static final HangarConfig config = StaticContextAccessor.getBean(HangarConfig.class);

    private final String name;

    // @el(root: String)
    private final @Validate(SpEL = "@validate.regex(#root, @hangarConfig.urlRegex)", message = "validation.invalidUrl") String url;

    // @el(root: String)
    private final @Validate(SpEL = "@validate.required(#root)") String type;

    @JdbiConstructor
    public ProjectLicense(final @Nullable String name, final @Nullable String url) {
        final int index = config.getLicenses().indexOf(name);
        if (name == null) {
            this.type = null;
            this.name = null;
        } else if (index > -1 && index < config.getLicenses().size() - 1) {
            this.name = null;
            this.type = name;
        } else {
            this.name = name;
            this.type = "(custom)";
        }
        this.url = url;
    }

    @JsonCreator
    public ProjectLicense(final @Nullable String name, final @Nullable String url, final @Nullable String type) {
        this.name = name;
        this.url = url;
        this.type = type;
    }

    public String getName() {
        return this.name;
    }

    public String getUrl() {
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
