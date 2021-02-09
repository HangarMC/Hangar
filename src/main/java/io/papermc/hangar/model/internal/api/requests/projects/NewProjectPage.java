package io.papermc.hangar.model.internal.api.requests.projects;

import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.constraints.NotBlank;

public class NewProjectPage {

    @NotBlank
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
