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
    @Validate(SpEL = "@validate.regex(#root, @hangarConfig.urlRegex)", message = "validation.invalidUrl")
    private final String url;

    // @el(root: String)
    @Validate(SpEL = "@validate.required(#root)")
    private final String type;

    @JdbiConstructor
    public ProjectLicense(@Nullable String name, @Nullable String url) {
        int index = config.getLicenses().indexOf(name);
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
    public ProjectLicense(@Nullable String name, @Nullable String url, @Nullable String type) {
        this.name = name;
        this.url = url;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return "ProjectLicense{" +
                "name='" + name + '\'' +
                ", url='" + url + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
