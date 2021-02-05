package io.papermc.hangar.serviceold;

import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.daoold.OrganizationDao;
import io.papermc.hangar.db.daoold.OrganizationMembersDao;
import io.papermc.hangar.db.daoold.ProjectDao;
import io.papermc.hangar.db.daoold.UserDao;
import io.papermc.hangar.db.daoold.UserOrganizationRolesDao;
import io.papermc.hangar.db.modelold.OrganizationMembersTable;
import io.papermc.hangar.db.modelold.OrganizationsTable;
import io.papermc.hangar.db.modelold.UserOrganizationRolesTable;
import io.papermc.hangar.db.modelold.UsersTable;
import io.papermc.hangar.model.common.Permission;
import io.papermc.hangar.modelold.viewhelpers.OrgMember;
import io.papermc.hangar.modelold.viewhelpers.OrganizationData;
import io.papermc.hangar.modelold.viewhelpers.ScopedOrganizationData;
import io.papermc.hangar.service.PermissionService;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

@Service
public class OrgService extends HangarService {

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
        if (currentUser.get().isPresent()) {
            return new ScopedOrganizationData(permissionService.getOrganizationPermissions(currentUser.get().get().getId(), org.getName()));
        }
        return new ScopedOrganizationData();
    }

    public OrganizationData getOrganizationData(UsersTable potentialOrg) {
        OrganizationsTable org = organizationDao.get().getByUserId(potentialOrg.getId());
        if (org == null) return null;
        return getOrganizationData(org, potentialOrg);
    }

    public OrganizationData getOrganizationData(OrganizationsTable org, @Nullable UsersTable orgUser) {
        UsersTable user = orgUser;
        if (orgUser == null) {
            user = userDao.get().getById(org.getUserId());
        }
        return new OrganizationData(org, user,organizationDao.get().getOrgMembers(org.getId()), projectDao.get().getProjectRoles(org.getUserId()));
    }
}
