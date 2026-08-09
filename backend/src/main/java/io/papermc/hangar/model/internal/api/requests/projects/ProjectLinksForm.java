package io.papermc.hangar.model.internal.api.requests.projects;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.papermc.hangar.controller.validations.Validate;
import io.papermc.hangar.model.api.project.settings.LinkSection;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class ProjectLinksForm {

    // @el(root: List<String>)
    private final @NotNull @Validate(SpEL = "@validate.max(#root, 4)", message = "Too many link sections") List<@Valid LinkSection> links;

    @JsonCreator
    public ProjectLinksForm(final List<LinkSection> links) {
        this.links = links;
    }

    public List<LinkSection> getLinks() {
        return this.links;
    }

    @Override
    public String toString() {
        return "ProjectLinksForm{" +
            "links=" + this.links +
            '}';
    }
}
