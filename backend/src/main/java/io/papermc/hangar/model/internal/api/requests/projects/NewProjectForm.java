package io.papermc.hangar.model.internal.api.requests.projects;

import io.papermc.hangar.controller.validations.Validate;
import io.papermc.hangar.model.api.project.ProjectSettings;
import io.papermc.hangar.model.common.projects.Category;
import javax.validation.constraints.NotNull;

public class NewProjectForm extends ProjectSettingsForm {

    private final long ownerId;

    // @el(root: String)
    private final @NotNull(message = "project.new.error.invalidName") @Validate(SpEL = "@validate.max(#root, @hangarConfig.projects.maxNameLen)", message = "project.new.error.tooLongName") @Validate(SpEL = "@validate.regex(#root, @hangarConfig.projects.nameRegex)", message = "project.new.error.invalidName") String name;

    // @el(root: String)
    private final @Validate(SpEL = "@validate.max(#root, @hangarConfig.pages.maxLen)", message = "page.new.error.maxLength") String pageContent;

    public NewProjectForm(final ProjectSettings settings, final Category category, final String description, final long ownerId, final String name, final String pageContent) {
        super(settings, category, description);
        this.ownerId = ownerId;
        this.name = name;
        this.pageContent = pageContent;
    }

    public long getOwnerId() {
        return this.ownerId;
    }

    public String getName() {
        return this.name;
    }

    public String getPageContent() {
        return this.pageContent;
    }

    @Override
    public String toString() {
        return "NewProjectForm{" +
                "ownerId=" + this.ownerId +
                ", name='" + this.name + '\'' +
                ", pageContent='" + this.pageContent + '\'' +
                "} " + super.toString();
    }
}
