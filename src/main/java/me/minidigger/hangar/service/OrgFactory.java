package me.minidigger.hangar.service;

import me.minidigger.hangar.config.hangar.HangarConfig;
import me.minidigger.hangar.db.dao.HangarDao;
import me.minidigger.hangar.db.dao.OrganizationDao;
import me.minidigger.hangar.db.dao.UserDao;
import me.minidigger.hangar.db.model.OrganizationsTable;
import me.minidigger.hangar.db.model.UsersTable;
import me.minidigger.hangar.model.Role;
import me.minidigger.hangar.model.viewhelpers.UserData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class OrgFactory {

    private final HangarDao<OrganizationDao> organizationDao;
    private final HangarConfig hangarConfig;
    private final UserService userService;
    private final RoleService roleService;

    /*TEMP*/
    private final HangarDao<UserDao> userDao;
    /*END TEMP*/

    @Autowired
    public OrgFactory(HangarDao<OrganizationDao> organizationDao, HangarConfig hangarConfig, UserService userService, RoleService roleService, HangarDao<UserDao> userDao) {
        this.organizationDao = organizationDao;
        this.hangarConfig = hangarConfig;
        this.userService = userService;
        this.roleService = roleService;
        this.userDao = userDao;
    }

    public OrganizationsTable createOrganization(String name, long ownerId, Map<Long, Role> members) {
        String dummyEmail = name.replaceAll("[^a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]", "") + '@' + hangarConfig.org.getDummyEmailDomain();
        // TODO create dummy user w/email
        /* TEMPORARY TODO REMOVE */
        UsersTable usersTable = userDao.get().insert(new UsersTable(name, name, dummyEmail, "I'm an organization", new int[]{ }, false, "en_US"));
        /* END TEMPORARY */
        OrganizationsTable org = new OrganizationsTable(name, ownerId, usersTable.getId()); // TODO userId will be from result of dummy user creation
        org = organizationDao.get().insert(org);
        long orgId = org.getId();
        UserData orgUser = userService.getUserData(usersTable.getId()); // TODO userId will be from result of dummy user creation
        roleService.addGlobalRole(orgUser.getUser().getId(), Role.ORGANIZATION.getRoleId());
        roleService.addOrgMemberRole(orgId, ownerId, Role.ORGANIZATION_OWNER, true);
        members.forEach((memberId, role) -> roleService.addOrgMemberRole(orgId, memberId, role, false));
        // TODO notifications for members
        return org;
    }
}
