package me.minidigger.hangar.service;

import me.minidigger.hangar.db.dao.HangarDao;
import me.minidigger.hangar.db.dao.OrganizationDao;
import me.minidigger.hangar.db.dao.OrganizationMembersDao;
import me.minidigger.hangar.db.dao.UserDao;
import me.minidigger.hangar.db.dao.UserOrganizationRolesDao;
import me.minidigger.hangar.db.model.OrganizationMembersTable;
import me.minidigger.hangar.db.model.OrganizationsTable;
import me.minidigger.hangar.db.model.UserOrganizationRolesTable;
import me.minidigger.hangar.db.model.UsersTable;
import me.minidigger.hangar.model.Permission;
import me.minidigger.hangar.model.viewhelpers.OrgMember;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class OrgService {

    private final HangarDao<OrganizationDao> organizationDao;
    private final HangarDao<UserDao> userDao;
    private final HangarDao<UserOrganizationRolesDao> userOrganizationRolesDao;
    private final HangarDao<OrganizationMembersDao> organizationMembersDao;

    @Autowired
    public OrgService(HangarDao<OrganizationDao> organizationDao, HangarDao<UserDao> userDao, HangarDao<UserOrganizationRolesDao> userOrganizationRolesDao, HangarDao<OrganizationMembersDao> organizationMembersDao) {
        this.organizationDao = organizationDao;
        this.userDao = userDao;
        this.userOrganizationRolesDao = userOrganizationRolesDao;
        this.organizationMembersDao = organizationMembersDao;
    }

    public List<OrganizationsTable> getOrgsWithPerm(UsersTable user, Permission permission) {
        Map<OrganizationsTable, UserOrganizationRolesTable> orgs = organizationDao.get().getUserOrganizationsAndRoles(user.getId());

        return List.of(new OrganizationsTable("TestOrg", -1, 4));
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
}
