package io.papermc.hangar.modelold.viewhelpers;

import io.papermc.hangar.db.customtypes.RoleCategory;
import io.papermc.hangar.db.modelold.OrganizationsTable;
import io.papermc.hangar.db.modelold.ProjectsTable;
import io.papermc.hangar.db.modelold.UserOrganizationRolesTable;
import io.papermc.hangar.db.modelold.UserProjectRolesTable;
import io.papermc.hangar.db.modelold.UsersTable;

import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class OrganizationData extends JoinableData<UserOrganizationRolesTable, OrganizationsTable> {

    private final Map<UserProjectRolesTable, ProjectsTable> projectRoles;

    public OrganizationData(OrganizationsTable org, UsersTable orgUser, Map<UserOrganizationRolesTable, UsersTable> members, Map<UserProjectRolesTable, ProjectsTable> projectRoles) {
        super(org, org.getOwnerId(), orgUser.getName(), members, RoleCategory.ORGANIZATION);
        this.projectRoles = projectRoles;
    }

    public OrganizationsTable getOrg() {
        return this.joinable;
    }

    public Map<UsersTable, UserRole<UserOrganizationRolesTable>> adminTable() {
        return getMembers().entrySet().stream().collect(Collectors.toMap(Entry::getValue, entry -> new UserRole<>(entry.getKey())));
    }

    public Map<UserProjectRolesTable, ProjectsTable> getProjectRoles() {
        return projectRoles;
    }
}
