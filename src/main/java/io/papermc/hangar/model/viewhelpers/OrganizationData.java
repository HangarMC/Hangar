package io.papermc.hangar.model.viewhelpers;

import io.papermc.hangar.db.customtypes.RoleCategory;
import io.papermc.hangar.db.model.OrganizationsTable;
import io.papermc.hangar.db.model.ProjectsTable;
import io.papermc.hangar.db.model.UserOrganizationRolesTable;
import io.papermc.hangar.db.model.UserProjectRolesTable;
import io.papermc.hangar.db.model.UsersTable;

import java.util.Map;

public class OrganizationData extends JoinableData<UserOrganizationRolesTable, OrganizationsTable> {

    private final Map<UserProjectRolesTable, ProjectsTable> projectRoles;

    public OrganizationData(OrganizationsTable org, Map<UserOrganizationRolesTable, UsersTable> members, Map<UserProjectRolesTable, ProjectsTable> projectRoles) {
        super(org, org.getOwnerId(), members, RoleCategory.ORGANIZATION);
        this.projectRoles = projectRoles;
    }

    public OrganizationsTable getOrg() {
        return this.joinable;
    }

    public Map<UserProjectRolesTable, ProjectsTable> getProjectRoles() {
        return projectRoles;
    }
}
