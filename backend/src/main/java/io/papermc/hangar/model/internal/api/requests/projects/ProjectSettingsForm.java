package io.papermc.hangar.model.internal.api.requests.projects;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.papermc.hangar.controller.validations.Validate;
import io.papermc.hangar.model.api.project.settings.ProjectSettings;
import io.papermc.hangar.model.common.projects.Category;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public class ProjectSettingsForm {

    @Valid
    private final ProjectSettings settings;
    @NotNull(message = "project.new.error.noCategory")
    private final Category category;

    // @el(root: String)
    @NotNull(message = "project.new.error.noDescription")
    private final @Validate(SpEL = "@validate.max(#root, @hangarConfig.projects.maxDescLen)", message = "project.new.error.tooLongDesc") String description;

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
