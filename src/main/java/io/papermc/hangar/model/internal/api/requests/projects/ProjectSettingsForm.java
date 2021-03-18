package io.papermc.hangar.model.internal.api.requests.projects;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.papermc.hangar.controller.validations.Validate;
import io.papermc.hangar.model.api.project.ProjectSettings;
import io.papermc.hangar.model.common.projects.Category;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class ProjectSettingsForm {

    @Valid
    private final ProjectSettings settings;
    @NotNull(message = "project.new.error.noCategory")
    private final Category category;
    @NotNull(message = "project.new.error.noDescription")
    @Validate(SpEL = "#root.length le @hangarConfig.projects.maxDescLen", message = "project.new.error.tooLongDesc")
    private final String description;

    @JsonCreator
    public ProjectSettingsForm(ProjectSettings settings, Category category, String description) {
        this.settings = settings;
        this.category = category;
        this.description = description;
    }

    public ProjectSettings getSettings() {
        return settings;
    }

    public Category getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "ProjectSettingsForm{" +
                "settings=" + settings +
                ", category=" + category +
                ", description='" + description + '\'' +
                '}';
    }
}
