package io.papermc.hangar.model.internal.api.responses;

import io.papermc.hangar.model.db.OrganizationTable;
import io.papermc.hangar.model.db.projects.ProjectOwner;

public class PossibleProjectOwner implements ProjectOwner {

    private final long id;
    private final long userId;
    private final String name;
    private final boolean isOrganization;

    public PossibleProjectOwner(final ProjectOwner projectOwner) {
        this.id = projectOwner.getId();
        this.userId = projectOwner.getUserId();
        this.name = projectOwner.getName();
        this.isOrganization = projectOwner instanceof OrganizationTable;
    }

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public long getUserId() {
        return this.userId;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public boolean isOrganization() {
        return this.isOrganization;
    }

    @Override
    public String toString() {
        return "PossibleProjectOwner{" +
            "id=" + this.id +
            ", userId=" + this.userId +
            ", name='" + this.name + '\'' +
            ", isOrganization=" + this.isOrganization +
            '}';
    }
}
