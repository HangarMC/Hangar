package io.papermc.hangar.controllerold.forms.objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.validation.annotation.Validated;

@Validated
public class ProjectLinks {

    private final String homepage;
    private final String issues;
    private final String source;
    private final String support;

    @JsonCreator(mode = Mode.PROPERTIES)
    public ProjectLinks(@JsonProperty("homepage") String homepage,
                        @JsonProperty("issues") String issues,
                        @JsonProperty("source") String source,
                        @JsonProperty("support") String support) {
        this.homepage = homepage;
        this.issues = issues;
        this.source = source;
        this.support = support;
    }

    public String getHomepage() {
        return homepage;
    }

    public String getIssues() {
        return issues;
    }

    public String getSource() {
        return source;
    }

    public String getSupport() {
        return support;
    }

    @Override
    public String toString() {
        return "ProjectLinks{" +
                "homepage='" + homepage + '\'' +
                ", issues='" + issues + '\'' +
                ", source='" + source + '\'' +
                ", support='" + support + '\'' +
                '}';
    }
}
