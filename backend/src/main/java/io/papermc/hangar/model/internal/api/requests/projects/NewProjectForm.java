package io.papermc.hangar.model.internal.api.requests.projects;

import io.papermc.hangar.controller.validations.Validate;
import io.papermc.hangar.model.api.project.ProjectSettings;
import io.papermc.hangar.model.common.projects.Category;
import javax.validation.constraints.NotNull;

public class NewProjectForm extends ProjectSettingsForm {

    private final long ownerId;

    // @el(root: String)
    @NotNull(message = "project.new.error.invalidName")
    @Validate(SpEL = "@validate.max(#root, @hangarConfig.projects.maxNameLen)", message = "project.new.error.tooLongName")
    @Validate(SpEL = "@validate.regex(#root, @hangarConfig.projects.nameRegex)", message = "project.new.error.invalidName")
    private final String name;

    // @el(root: String)
    @Validate(SpEL = "@validate.max(#root, @hangarConfig.pages.maxLen)", message = "page.new.error.maxLength")
    private final String pageContent;

    public NewProjectForm(ProjectSettings settings, Category category, String description, long ownerId, String name, String pageContent) {
        super(settings, category, description);
        this.ownerId = ownerId;
        this.name = name;
        this.pageContent = pageContent;
    }

    public long getOwnerId() {
        return ownerId;
    }

    public String getName() {
        return name;
    }

    public String getPageContent() {
        return pageContent;
    }

    @Override
    public String toString() {
        return "NewProjectForm{" +
                "ownerId=" + ownerId +
                ", name='" + name + '\'' +
                ", pageContent='" + pageContent + '\'' +
                "} " + super.toString();
    }
}
