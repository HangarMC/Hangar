package io.papermc.hangar.service;

import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.OrganizationDao;
import io.papermc.hangar.db.dao.OrganizationMembersDao;
import io.papermc.hangar.db.dao.ProjectDao;
import io.papermc.hangar.db.dao.UserDao;
import io.papermc.hangar.db.dao.UserOrganizationRolesDao;
import io.papermc.hangar.db.model.OrganizationMembersTable;
import io.papermc.hangar.db.model.OrganizationsTable;
import io.papermc.hangar.db.model.UserOrganizationRolesTable;
import io.papermc.hangar.db.model.UsersTable;
import io.papermc.hangar.model.Permission;
import io.papermc.hangar.model.viewhelpers.OrgMember;
import io.papermc.hangar.model.viewhelpers.OrganizationData;
import io.papermc.hangar.model.viewhelpers.ScopedOrganizationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

@Service
public class OrgService {

    private final PermissionService permissionService;
    private final UserService userService;
    private final HangarDao<OrganizationDao> organizationDao;
    private final HangarDao<UserDao> userDao;
    private final HangarDao<ProjectDao> projectDao;
    private final HangarDao<UserOrganizationRolesDao> userOrganizationRolesDao;
    private final HangarDao<OrganizationMembersDao> organizationMembersDao;

    @Autowired
    public OrgService(PermissionService permissionService, @Lazy UserService userService, HangarDao<OrganizationDao> organizationDao, HangarDao<UserDao> userDao, HangarDao<ProjectDao> projectDao, HangarDao<UserOrganizationRolesDao> userOrganizationRolesDao, HangarDao<OrganizationMembersDao> organizationMembersDao) {
        this.permissionService = permissionService;
        this.userService = userService;
        this.organizationDao = organizationDao;
        this.userDao = userDao;
        this.projectDao = projectDao;
        this.userOrganizationRolesDao = userOrganizationRolesDao;
        this.organizationMembersDao = organizationMembersDao;
    }

    public List<OrganizationsTable> getOrgsWithPerm(UsersTable user, Permission permission) {
        Map<OrganizationsTable, Permission> orgs = organizationDao.get().getUserOrganizationsAndPermissions(user.getId());
        return orgs.entrySet().stream().filter(entry -> entry.getValue().has(permission)).map(Entry::getKey).collect(Collectors.toList());
    }

    public List<OrganizationsTable> getUserOwnedOrgs(long userId) {
        return organizationDao.get().getUserOwnedOrgs(userId);
    }

    public List<OrganizationsTable> getUsersOrgs(long userId) {
        return organizationDao.get().getUserOrgs(userId);
    }

    public OrgMember getOrganizationMember(long orgId, long userId) {
        UsersTable user = userDao.get().getById(userId);
        OrganizationsTable org = organizationDao.get().getById(orgId);
        List<UserOrganizationRolesTable> orgRoles = userOrganizationRolesDao.get().getUserRoles(orgId, userId);
        if (user == null || org == null) return null;
        return new OrgMember(user, org, orgRoles);
    }

    public OrgMember addOrgMember(long orgId, long userId) {
        organizationMembersDao.get().insert(new OrganizationMembersTable(userId, orgId));
        return getOrganizationMember(orgId, userId);
    }

    public OrganizationsTable getOrganization(String name) {
        return organizationDao.get().getByUserName(name);
    }

    public ScopedOrganizationData getScopedOrganizationData(OrganizationsTable org) {
        if (userService.getCurrentUser() != null) {
            return new ScopedOrganizationData(permissionService.getOrganizationPermissions(userService.getCurrentUser(), org.getName(), false));
        }
        return new ScopedOrganizationData();
    }

    public OrganizationData getOrganizationData(OrganizationsTable org) {
        return new OrganizationData(org, organizationDao.get().getOrgMembers(org.getId()), projectDao.get().getProjectRoles(org.getId()));
    }
}
