package io.papermc.hangar.service.internal.organizations;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.db.dao.internal.HangarOrganizationsDAO;
import io.papermc.hangar.db.dao.internal.table.OrganizationDAO;
import io.papermc.hangar.db.dao.internal.table.UserDAO;
import io.papermc.hangar.db.dao.internal.table.roles.OrganizationRolesDAO;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.common.Permission;
import io.papermc.hangar.model.db.OrganizationTable;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.model.db.roles.OrganizationRoleTable;
import io.papermc.hangar.model.internal.HangarOrganization;
import io.papermc.hangar.service.PermissionService;
import io.papermc.hangar.service.internal.perms.members.OrganizationMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class OrganizationService extends HangarComponent {

    private final HangarOrganizationsDAO hangarOrganizationsDAO;
    private final OrganizationRolesDAO organizationRolesDAO;
    private final OrganizationDAO organizationDAO;
    private final UserDAO userDAO;
    private final PermissionService permissionService;
    private final OrganizationMemberService organizationMemberService;

    @Autowired
    public OrganizationService(HangarOrganizationsDAO hangarOrganizationsDAO, OrganizationRolesDAO organizationRolesDAO, OrganizationDAO organizationDAO, UserDAO userDAO, PermissionService permissionService, OrganizationMemberService organizationMemberService) {
        this.hangarOrganizationsDAO = hangarOrganizationsDAO;
        this.organizationRolesDAO = organizationRolesDAO;
        this.organizationDAO = organizationDAO;
        this.userDAO = userDAO;
        this.permissionService = permissionService;
        this.organizationMemberService = organizationMemberService;
    }

    public OrganizationTable getOrganizationTable(String name) {
        return organizationDAO.getByName(name);
    }

    public List<OrganizationTable> getOrganizationTablesWithPermission(long userId, Permission permission) {
        return organizationDAO.getOrganizationsWithPermission(userId, permission);
    }

    public List<OrganizationTable> getOrganizationsOwnedBy(long ownerId) {
        return organizationDAO.getOrganizationsOwnedBy(ownerId);
    }

    public HangarOrganization getHangarOrganization(String name) {
        OrganizationTable organizationTable = organizationDAO.getByName(name);
        if (organizationTable == null) {
            throw new HangarApiException(HttpStatus.NOT_FOUND);
        }
        UserTable owner = userDAO.getUserTable(organizationTable.getOwnerId());
        var members = hangarOrganizationsDAO.getOrganizationMembers(organizationTable.getId(), getHangarUserId(), permissionService.getOrganizationPermissions(getHangarUserId(), organizationTable.getId()).has(Permission.ManageOrganizationMembers));
        return new HangarOrganization(organizationTable.getId(), owner, members);
    }

    public Map<String, OrganizationRoleTable> getUserOrganizationRoles(String user, boolean includeHidden) {
        Map<String, OrganizationRoleTable> roles = organizationRolesDAO.getUserOrganizationRoles(user, getHangarUserId());
        if (!includeHidden) {
            Map<String, Boolean> visibility = organizationMemberService.getUserOrganizationMembershipVisibility(user);
            roles.keySet().removeIf(org -> Boolean.TRUE.equals(visibility.getOrDefault(org, true)));
        }
        return roles;
    }

    public long getUserOrganizationCount(long userId) {
        return organizationDAO.getOrganizationCount(userId);
    }
}
