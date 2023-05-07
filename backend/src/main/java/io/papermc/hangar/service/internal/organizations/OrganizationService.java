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
import io.papermc.hangar.model.internal.user.JoinableMember;
import io.papermc.hangar.service.PermissionService;
import io.papermc.hangar.components.images.service.AvatarService;
import io.papermc.hangar.service.internal.perms.members.OrganizationMemberService;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class OrganizationService extends HangarComponent {

    private final HangarOrganizationsDAO hangarOrganizationsDAO;
    private final OrganizationRolesDAO organizationRolesDAO;
    private final OrganizationDAO organizationDAO;
    private final UserDAO userDAO;
    private final PermissionService permissionService;
    private final OrganizationMemberService organizationMemberService;
    private final AvatarService avatarService;

    @Autowired
    public OrganizationService(final HangarOrganizationsDAO hangarOrganizationsDAO, final OrganizationRolesDAO organizationRolesDAO, final OrganizationDAO organizationDAO, final UserDAO userDAO, final PermissionService permissionService, final OrganizationMemberService organizationMemberService, @Lazy final AvatarService avatarService) {
        this.hangarOrganizationsDAO = hangarOrganizationsDAO;
        this.organizationRolesDAO = organizationRolesDAO;
        this.organizationDAO = organizationDAO;
        this.userDAO = userDAO;
        this.permissionService = permissionService;
        this.organizationMemberService = organizationMemberService;
        this.avatarService = avatarService;
    }

    public OrganizationTable getOrganizationTable(final long id) {
        return this.organizationDAO.getById(id);
    }

    public OrganizationTable getOrganizationTableByUser(final long userId) {
        return this.organizationDAO.getByUserId(userId);
    }

    public OrganizationTable getOrganizationTable(final String name) {
        return this.organizationDAO.getByName(name);
    }

    public List<OrganizationTable> getOrganizationTablesWithPermission(final long userId, final Permission permission) {
        return this.organizationDAO.getOrganizationsWithPermission(userId, permission);
    }

    public OrganizationTable getOrganizationTableWithPermission(final long userId, final long organizationUserId, final Permission permission) {
        return this.organizationDAO.getOrganizationWithPermission(userId, organizationUserId, permission);
    }

    public List<OrganizationTable> getOrganizationsOwnedBy(final long ownerId) {
        return this.organizationDAO.getOrganizationsOwnedBy(ownerId);
    }

    public HangarOrganization getHangarOrganization(final String name) {
        final OrganizationTable organizationTable = this.organizationDAO.getByName(name);
        if (organizationTable == null) {
            throw new HangarApiException(HttpStatus.NOT_FOUND);
        }
        final UserTable owner = this.userDAO.getUserTable(organizationTable.getOwnerId());
        final List<JoinableMember<OrganizationRoleTable>> members = this.hangarOrganizationsDAO.getOrganizationMembers(organizationTable.getId(), this.getHangarUserId(), this.permissionService.getOrganizationPermissions(this.getHangarUserId(), organizationTable.getId()).has(Permission.ManageOrganizationMembers));
        members.forEach(member -> member.setAvatarUrl(this.avatarService.getUserAvatarUrl(member.getUser())));
        return new HangarOrganization(organizationTable.getId(), owner, members);
    }

    public Map<String, OrganizationRoleTable> getUserOrganizationRoles(final String user, final boolean includeHidden) {
        final Map<String, OrganizationRoleTable> roles = this.organizationRolesDAO.getUserOrganizationRoles(user, this.getHangarUserId());
        if (!includeHidden) {
            final Map<String, Boolean> visibility = this.organizationMemberService.getUserOrganizationMembershipVisibility(user);
            roles.keySet().removeIf(org -> Boolean.TRUE.equals(visibility.getOrDefault(org, true)));
        }
        roles.values().forEach(org -> org.setAvatarUrl(this.avatarService.getUserAvatarUrl(org.getUuid())));
        return roles;
    }

    public long getUserOrganizationCount(final long userId) {
        return this.organizationDAO.getOrganizationCount(userId);
    }
}
