package io.papermc.hangar.model.api.project;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import io.papermc.hangar.controller.validations.Validate;
import io.papermc.hangar.util.StringUtils;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

import java.util.Map;

public class ProjectLicense {

    private final String name;
    @Validate(SpEL = "@validate.optionalRegex(#root, @hangarConfig.urlRegex)", message = "validation.invalidUrl")
    private final String url;

    @JdbiConstructor
    public ProjectLicense(String name, String url) {
        this.name = name;
        this.url = url;
    }

    @JsonCreator(mode = Mode.DELEGATING)
    public ProjectLicense(Map<String, String> map) {
        String licenseName = StringUtils.stringOrNull(map.get("name"));
        if (licenseName == null) {
            licenseName = map.get("type");
        }
        this.name = licenseName;
        this.url = map.get("url");
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public String toString() {
        return "ProjectLicense{" +
                "name='" + name + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
