package io.papermc.hangar.model.api.project.version;

import io.papermc.hangar.model.Named;

public class PluginDependency implements Named {

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
        return "Dependency{" +
                "name='" + name + '\'' +
                ", required=" + required +
                ", projectId=" + projectId +
                ", externalUrl='" + externalUrl + '\'' +
                '}';
    }
}
