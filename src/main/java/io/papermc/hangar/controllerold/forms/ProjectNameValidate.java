package io.papermc.hangar.controllerold.forms;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

public class ProjectNameValidate {

    private final long ownerId;

    @NotNull
    private final String projectName;

    public ProjectNameValidate(@JsonProperty(value = "user", required = true) long ownerId, @JsonProperty(value = "name", required = true) @NotNull String projectName) {
        this.ownerId = ownerId;
        this.projectName = projectName;
    }

    public long getOwnerId() {
        return ownerId;
    }

    public @NotNull String getProjectName() {
        return projectName;
    }

    @Override
    public String toString() {
        return "ProjectNameValidate{" +
                "userName='" + ownerId + '\'' +
                ", projectName='" + projectName + '\'' +
                '}';
    }
}
