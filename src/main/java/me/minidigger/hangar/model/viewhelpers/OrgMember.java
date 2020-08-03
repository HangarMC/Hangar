package me.minidigger.hangar.model.viewhelpers;

import me.minidigger.hangar.db.model.OrganizationsTable;
import me.minidigger.hangar.db.model.UserOrganizationRolesTable;
import me.minidigger.hangar.db.model.UsersTable;
import me.minidigger.hangar.model.Role;

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
