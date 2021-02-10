package io.papermc.hangar.model.internal.api.requests.projects;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.papermc.hangar.controller.validations.Validate;

import javax.validation.constraints.NotBlank;

public class NewProjectPage {

    @NotBlank
    @Validate(SpEL = "#root.length() ge @hangarConfig.pages.minNameLen", message = "page.new.error.name.minLength")
    @Validate(SpEL = "#root.length() le @hangarConfig.pages.maxNameLen", message = "page.new.error.name.maxLength")
    @Validate(SpEL = "#root matches @hangarConfig.pages.nameRegex", message = "page.new.error.name.invalidChars")
    private final String name;
    private final Long parentId;

    @JsonCreator
    public NewProjectPage(@NotBlank String name, Long parentId) {
        this.name = name;
        this.parentId = parentId;
    }

    public String getName() {
        return name;
    }


    public Long getParentId() {
        return parentId;
    }

    @Override
    public String toString() {
        return "NewProjectPage{" +
                "name='" + name + '\'' +
                ", parentId=" + parentId +
                '}';
    }
}
