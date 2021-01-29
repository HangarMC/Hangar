package io.papermc.hangar.modelold.viewhelpers;

import io.papermc.hangar.db.modelold.OrganizationsTable;
import io.papermc.hangar.db.modelold.UserOrganizationRolesTable;
import io.papermc.hangar.db.modelold.UsersTable;
import io.papermc.hangar.modelold.Role;

import java.util.ArrayList;
import java.util.List;

public class OrgMember {

    private UsersTable user;
    private OrganizationsTable org;
    private List<UserOrganizationRolesTable> userRoles;
    private List<Role> role = new ArrayList<>();

    public OrgMember(UsersTable user, OrganizationsTable org, List<UserOrganizationRolesTable> userRoles) {
        this.user = user;
        this.org = org;
        this.userRoles = userRoles;
        if (!userRoles.isEmpty()) {
            userRoles.forEach(userRole -> role.add(Role.valueOf(userRole.getRoleType().toUpperCase())));
        }
    }

    public UsersTable getUser() {
        return user;
    }

    public OrganizationsTable getOrg() {
        return org;
    }

    public List<UserOrganizationRolesTable> getUserOrganizationRolesTables() {
        return userRoles;
    }

    public List<Role> getRoles() {
        return role;
    }
}
