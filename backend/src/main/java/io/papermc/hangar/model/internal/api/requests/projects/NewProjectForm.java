package io.papermc.hangar.model.internal.api.requests.projects;

import io.papermc.hangar.controller.validations.Validate;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.api.project.settings.ProjectSettings;
import io.papermc.hangar.model.common.projects.Category;
import jakarta.validation.constraints.NotNull;
import org.jetbrains.annotations.Nullable;

public class NewProjectForm extends ProjectSettingsForm {

    private final long ownerId;

    // @el(root: String)
    @NotNull(message = "project.new.error.invalidName")
    private final @Validate(SpEL = "@validate.max(#root, @hangarConfig.projects.maxNameLen)", message = "project.new.error.tooLongName") @Validate(SpEL = "@validate.regex(#root, @hangarConfig.projects.nameRegex)", message = "project.new.error.invalidName") String name;

    // @el(root: String)
    private final @Validate(SpEL = "@validate.max(#root, @hangarConfig.pages.maxLen)", message = "page.new.error.maxLength") String pageContent;

    private final @Nullable String avatarUrl;

    public NewProjectForm(final ProjectSettings settings, final Category category, final String description, final long ownerId, final String name, final String pageContent, final @Nullable String avatarUrl) {
        super(settings, category, description);
        if (category == Category.UNDEFINED) {
            throw new HangarApiException("Category must be defined");
        }
        this.ownerId = ownerId;
        this.name = name;
        this.pageContent = pageContent;
        this.avatarUrl = avatarUrl;
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

    public String getAvatarUrl() {
        return this.avatarUrl;
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
