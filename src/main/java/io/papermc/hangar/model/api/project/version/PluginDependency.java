package io.papermc.hangar.model.api.project.version;

import io.papermc.hangar.controller.validations.AtLeastOneNotNull;
import io.papermc.hangar.model.Named;

import javax.validation.constraints.NotBlank;

@AtLeastOneNotNull(fieldNames = {"projectId", "externalUrl"}, message = "Must specify a projectId or external URL for a dependency")
public class PluginDependency implements Named {

    @NotBlank(message = "Must have a dependency name")
    private final String name;
    private final boolean required;
    private final Long projectId;
    private final String externalUrl;

    public PluginDependency(String name, boolean required, Long projectId, String externalUrl) {
        this.name = name;
        this.required = required;
        this.projectId = projectId;
        this.externalUrl = externalUrl;
    }

    @Override
    public String getName() {
        return name;
    }

    public boolean isRequired() {
        return required;
    }

    public Long getProjectId() {
        return projectId;
    }

    public String getExternalUrl() {
        return externalUrl;
    }

    @Override
    public String toString() {
        return "PluginDependency{" +
                "name='" + name + '\'' +
                ", required=" + required +
                ", projectId=" + projectId +
                ", externalUrl='" + externalUrl + '\'' +
                '}';
    }
}
