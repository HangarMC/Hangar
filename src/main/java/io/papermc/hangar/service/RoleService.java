package io.papermc.hangar.service;

import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.ProjectMembersDao;
import io.papermc.hangar.db.dao.RoleDao;
import io.papermc.hangar.db.dao.UserGlobalRolesDao;
import io.papermc.hangar.db.dao.UserOrganizationRolesDao;
import io.papermc.hangar.db.dao.UserProjectRolesDao;
import io.papermc.hangar.db.model.ProjectMembersTable;
import io.papermc.hangar.db.model.ProjectsTable;
import io.papermc.hangar.db.model.RolesTable;
import io.papermc.hangar.db.model.UserGlobalRolesTable;
import io.papermc.hangar.db.model.UserOrganizationRolesTable;
import io.papermc.hangar.db.model.UserProjectRolesTable;
import io.papermc.hangar.model.viewhelpers.OrgMember;
import org.postgresql.shaded.com.ongres.scram.common.util.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import io.papermc.hangar.model.Role;

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
        if (admin != null && admin.getRole() == Role.HANGAR_ADMIN) {
            log.info("Skipping role init");
            return;
        }

        log.info("Initializing roles (first start only)");
        for (Role role : Role.values()) {
            roleDao.get().insert(RolesTable.fromRole(role));
        }
    }

    public UserProjectRolesTable getUserProjectRole(long id) {
        return userProjectRolesDao.get().getById(id);
    }

    public void addRole(ProjectsTable projectsTable, long userId, Role role, boolean isAccepted) {
        if (userProjectRolesDao.get().getByProjectAndUser(projectsTable.getId(), userId) == null) {
            addMember(projectsTable.getId(), userId);
        }
        userProjectRolesDao.get().insert(new UserProjectRolesTable(userId, role.getValue(), projectsTable.getId(), isAccepted));
    }

    public void updateRole(ProjectsTable projectsTable, long userId, Role role) {
        UserProjectRolesTable userProjectRole = userProjectRolesDao.get().getByProjectAndUser(projectsTable.getId(), userId);
        if (userProjectRole == null) return;
        userProjectRole.setRoleType(role.getValue());
        userProjectRolesDao.get().update(userProjectRole);
    }

    public void updateRole(UserProjectRolesTable userProjectRolesTable) {
        userProjectRolesDao.get().update(userProjectRolesTable);
    }

    public void removeRole(ProjectsTable projectsTable, long userId) {
        userProjectRolesDao.get().delete(projectsTable.getId(), userId);
    }

    public void removeRole(UserProjectRolesTable userProjectRolesTable) {
        userProjectRolesDao.get().delete(userProjectRolesTable.getProjectId(), userProjectRolesTable.getUserId());
    }

    public void addMember(long projectId, long userId) {
        projectMembersDao.get().insert(new ProjectMembersTable(projectId, userId));
    }

    public int removeMember(ProjectsTable projectsTable, long userId) {
        removeRole(projectsTable, userId);
        return projectMembersDao.get().delete(projectsTable.getId(), userId);
    }

    public void addGlobalRole(long userId, long roleId) {
        userGlobalRolesDao.get().insert(new UserGlobalRolesTable(userId, roleId));
    }


    public void removeGlobalRole(long userId, long roleId) {
        userGlobalRolesDao.get().delete(new UserGlobalRolesTable(userId, roleId));
    }

    public void removeAllGlobalRoles(long userId) {
        userGlobalRolesDao.get().deleteAll(userId);
    }

    public void addOrgMemberRole(long orgId, long userId, Role role, boolean accepted) {
        OrgMember orgMember = orgService.getOrganizationMember(orgId, userId);
        if (orgMember == null || orgMember.getRoles().size() == 0) { // add org member
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
