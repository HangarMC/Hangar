package io.papermc.hangar.model.internal.api.responses;

import io.papermc.hangar.model.db.OrganizationTable;
import io.papermc.hangar.model.db.projects.ProjectOwner;

public class PossibleProjectOwner {

    private final long id;
    private final long userId;
    private final String name;
    private final boolean isOrganization;

    public PossibleProjectOwner(ProjectOwner projectOwner) {
        this.id = projectOwner.getId();
        this.userId = projectOwner.getUserId();
        this.name = projectOwner.getName();
        this.isOrganization = projectOwner instanceof OrganizationTable;
    }

    public long getId() {
        return id;
    }

    public long getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public boolean isOrganization() {
        return isOrganization;
    }

    @Override
    public String toString() {
        return "PossibleProjectOwner{" +
                "id=" + id +
                ", userId=" + userId +
                ", name='" + name + '\'' +
                ", isOrganization=" + isOrganization +
                '}';
    }
}
