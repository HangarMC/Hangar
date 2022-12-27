package io.papermc.hangar.model.internal.api.requests.projects;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.papermc.hangar.controller.validations.Validate;
import io.papermc.hangar.model.api.project.ProjectSettings;
import io.papermc.hangar.model.common.projects.Category;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class ProjectSettingsForm {

    private final @Valid ProjectSettings settings;
    private final @NotNull(message = "project.new.error.noCategory") Category category;

    // @el(root: String)
    private final @NotNull(message = "project.new.error.noDescription") @Validate(SpEL = "@validate.max(#root, @hangarConfig.projects.maxDescLen)", message = "project.new.error.tooLongDesc") String description;

    @JsonCreator
    public ProjectSettingsForm(final ProjectSettings settings, final Category category, final String description) {
        this.settings = settings;
        this.category = category;
        this.description = description;
    }

    public ProjectSettings getSettings() {
        return this.settings;
    }

    public Category getCategory() {
        return this.category;
    }

    public String getDescription() {
        return this.description;
    }

    @Override
    public String toString() {
        return "ProjectSettingsForm{" +
            "settings=" + this.settings +
            ", category=" + this.category +
            ", description='" + this.description + '\'' +
            '}';
    }
}
