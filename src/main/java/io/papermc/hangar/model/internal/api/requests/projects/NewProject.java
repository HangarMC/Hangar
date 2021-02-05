package io.papermc.hangar.model.internal.api.requests.projects;

import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.constraints.Size;

public class NewProject {

    private final long ownerId;
    @Size(min = 7, message = "Name has to be at least 7 characters")
    private final String name;
    private final String description;

    @JsonCreator
    public NewProject(long ownerId, String name, String description) {
        this.ownerId = ownerId;
        this.name = name;
        this.description = description;
    }

    public long getOwnerId() {
        return ownerId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "NewProject{" +
                "ownerId=" + ownerId +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
