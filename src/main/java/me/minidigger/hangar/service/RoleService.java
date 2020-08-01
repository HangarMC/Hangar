package me.minidigger.hangar.service;

import me.minidigger.hangar.db.dao.UserOrganizationRolesDao;
import me.minidigger.hangar.db.model.UserOrganizationRolesTable;
import me.minidigger.hangar.model.viewhelpers.OrgMember;
import org.postgresql.shaded.com.ongres.scram.common.util.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import me.minidigger.hangar.db.dao.HangarDao;
import me.minidigger.hangar.db.dao.ProjectMembersDao;
import me.minidigger.hangar.db.dao.RoleDao;
import me.minidigger.hangar.db.dao.UserGlobalRolesDao;
import me.minidigger.hangar.db.dao.UserProjectRolesDao;
import me.minidigger.hangar.db.model.ProjectMembersTable;
import me.minidigger.hangar.db.model.ProjectsTable;
import me.minidigger.hangar.db.model.RolesTable;
import me.minidigger.hangar.db.model.UserGlobalRolesTable;
import me.minidigger.hangar.db.model.UserProjectRolesTable;
import me.minidigger.hangar.model.Role;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoleService {

    private static final Logger log = LoggerFactory.getLogger(RoleService.class);

    private final HangarDao<RoleDao> roleDao;
    private final HangarDao<UserProjectRolesDao> userProjectRolesDao;
    private final HangarDao<ProjectMembersDao> projectMembersDao;
    private final HangarDao<UserGlobalRolesDao> userGlobalRolesDao;
    private final HangarDao<UserOrganizationRolesDao> userOrganizationRolesDao;
    private final OrgService orgService;

    @Autowired
    public RoleService(HangarDao<RoleDao> roleDao, HangarDao<UserProjectRolesDao> userProjectRolesDao, HangarDao<ProjectMembersDao> projectMembersDao, HangarDao<UserGlobalRolesDao> userGlobalRolesDao, HangarDao<UserOrganizationRolesDao> userOrganizationRolesDao, OrgService orgService) {
        this.roleDao = roleDao;
        this.userProjectRolesDao = userProjectRolesDao;
        this.projectMembersDao = projectMembersDao;
        this.userGlobalRolesDao = userGlobalRolesDao;
        this.userOrganizationRolesDao = userOrganizationRolesDao;
        this.orgService = orgService;
        init();
    }

    public void init() {
        RolesTable admin = roleDao.get().getById(1);
        if(admin != null && admin.getRole().equals(Role.HANGAR_ADMIN)) {
            log.info("Skipping role init");
            return;
        }

        log.info("Initializing roles (first start only)");
        for (Role role : Role.values()) {
            roleDao.get().insert(RolesTable.fromRole(role));
        }
    }

    public void addRole(ProjectsTable projectsTable, long userId, Role role, boolean isAccepted) {
        boolean exists = userProjectRolesDao.get().getByProjectAndUser(projectsTable.getId(), userId) != null;
        if (!exists) {
            addMember(projectsTable.getId(), userId);
        }
        userProjectRolesDao.get().insert(new UserProjectRolesTable(userId, role.getValue(), projectsTable.getId(), isAccepted));
    }

    public void addMember(long projectId, long userId) {
        projectMembersDao.get().insert(new ProjectMembersTable(projectId, userId));
    }

    public void addGlobalRole(long userId, long roleId) {
        userGlobalRolesDao.get().insert(new UserGlobalRolesTable(userId, roleId));
    }


    public void removeGlobalRole(long userId, long roleId) {
        userGlobalRolesDao.get().delete(new UserGlobalRolesTable(userId, roleId));
    }

    public void addOrgMemberRole(long orgId, long userId, Role role, boolean accepted) {
        OrgMember orgMember = orgService.getOrganizationMember(orgId, userId);
        if (orgMember == null) { // add org member
            orgMember = orgService.addOrgMember(orgId, userId);
        }
        for (Role userRole : orgMember.getRoles()) {
            if (userRole == role) return; // no change
        }
        userOrganizationRolesDao.get().insert(new UserOrganizationRolesTable(userId, role.getValue(), orgId, accepted));
    }

    public List<Role> getGlobalRolesForUser(@Nullable Long userId, @Nullable String userName) {
        Preconditions.checkArgument(userId != null || userName != null, "One of (userId, userName) must be nonnull");
        return userGlobalRolesDao.get().getRolesByUserId(userId, userName).stream().map(adf -> Role.fromId(adf.getId())).collect(Collectors.toList());
    }
}
