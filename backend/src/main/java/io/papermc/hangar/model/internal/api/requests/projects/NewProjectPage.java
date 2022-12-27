package io.papermc.hangar.model.internal.api.requests.projects;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.papermc.hangar.controller.validations.Validate;
import javax.validation.constraints.NotBlank;

public class NewProjectPage {

    // @el(root: String)
    private final @NotBlank @Validate(SpEL = "@validate.min(#root, @hangarConfig.pages.minNameLen)", message = "page.new.error.name.minLength") @Validate(SpEL = "@validate.max(#root, @hangarConfig.pages.maxNameLen)", message = "page.new.error.name.maxLength") @Validate(SpEL = "@validate.regex(#root, @hangarConfig.pages.nameRegex)", message = "page.new.error.name.invalidChars") String name;
    private final Long parentId;

    @JsonCreator
    public NewProjectPage(final @NotBlank String name, final Long parentId) {
        this.name = name;
        this.parentId = parentId;
    }

    public String getName() {
        return this.name;
    }


    public Long getParentId() {
        return this.parentId;
    }

    @Override
    public String toString() {
        return "NewProjectPage{" +
                "name='" + this.name + '\'' +
                ", parentId=" + this.parentId +
                '}';
    }
}
