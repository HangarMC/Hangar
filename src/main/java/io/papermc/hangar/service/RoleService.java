package io.papermc.hangar.service;

import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.OrganizationMembersDao;
import io.papermc.hangar.db.dao.ProjectMembersDao;
import io.papermc.hangar.db.dao.UserGlobalRolesDao;
import io.papermc.hangar.db.dao.UserOrganizationRolesDao;
import io.papermc.hangar.db.dao.UserProjectRolesDao;
import io.papermc.hangar.db.modelold.OrganizationsTable;
import io.papermc.hangar.db.modelold.ProjectMembersTable;
import io.papermc.hangar.db.modelold.ProjectsTable;
import io.papermc.hangar.db.modelold.RoleTable;
import io.papermc.hangar.db.modelold.UserGlobalRolesTable;
import io.papermc.hangar.db.modelold.UserOrganizationRolesTable;
import io.papermc.hangar.db.modelold.UserProjectRolesTable;
import io.papermc.hangar.modelold.Role;
import io.papermc.hangar.modelold.viewhelpers.OrgMember;
import org.postgresql.shaded.com.ongres.scram.common.util.Preconditions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoleService {

    private final HangarDao<UserProjectRolesDao> userProjectRolesDao;
    private final HangarDao<ProjectMembersDao> projectMembersDao;
    private final HangarDao<UserGlobalRolesDao> userGlobalRolesDao;
    private final HangarDao<UserOrganizationRolesDao> userOrganizationRolesDao;
    private final HangarDao<OrganizationMembersDao> organizationMembersDao;
    private final OrgService orgService;

    @Autowired
    public RoleService(HangarDao<UserProjectRolesDao> userProjectRolesDao, HangarDao<ProjectMembersDao> projectMembersDao, HangarDao<UserGlobalRolesDao> userGlobalRolesDao, HangarDao<UserOrganizationRolesDao> userOrganizationRolesDao, HangarDao<OrganizationMembersDao> organizationMembersDao, OrgService orgService) {
        this.userProjectRolesDao = userProjectRolesDao;
        this.projectMembersDao = projectMembersDao;
        this.userGlobalRolesDao = userGlobalRolesDao;
        this.userOrganizationRolesDao = userOrganizationRolesDao;
        this.organizationMembersDao = organizationMembersDao;
        this.orgService = orgService;
    }

    public UserProjectRolesTable getUserProjectRole(long id) {
        return userProjectRolesDao.get().getById(id);
    }

    public UserOrganizationRolesTable getUserOrgRole(long id) {
        return userOrganizationRolesDao.get().getById(id);
    }

    public void addRole(ProjectsTable projectsTable, long userId, Role role, boolean isAccepted) {
        if (userProjectRolesDao.get().getByProjectAndUser(projectsTable.getId(), userId) == null) {
            addMember(projectsTable.getId(), userId);
        }
        userProjectRolesDao.get().insert(new UserProjectRolesTable(userId, role.getValue(), projectsTable.getId(), isAccepted));
    }

    public <R extends RoleTable> void updateRole(R roleTable) {
        if (roleTable instanceof UserProjectRolesTable) {
            this.updateRole((UserProjectRolesTable) roleTable);
        } else if (roleTable instanceof UserOrganizationRolesTable) {
            this.updateRole((UserOrganizationRolesTable) roleTable);
        }
    }

    public <R extends RoleTable> void removeRole(R roleTable) {
        if (roleTable instanceof UserProjectRolesTable) {
            this.removeRole( (UserProjectRolesTable) roleTable);
        } else if (roleTable instanceof UserOrganizationRolesTable) {
            this.removeRole(((UserOrganizationRolesTable) roleTable).getOrganizationId(), ((UserOrganizationRolesTable) roleTable).getUserId());
        }
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

    public void updateRole(OrganizationsTable organizationsTable, long userId, Role role) {
        UserOrganizationRolesTable userOrganizationRoles = userOrganizationRolesDao.get().getByOrgAndUser(organizationsTable.getId(), userId);
        if (userOrganizationRoles == null) return;
        userOrganizationRoles.setRoleType(role.getValue());
        userOrganizationRolesDao.get().update(userOrganizationRoles);
    }

    public void updateRole(UserOrganizationRolesTable userOrganizationRolesTable) {
        userOrganizationRolesDao.get().update(userOrganizationRolesTable);
    }

    public void removeRole(ProjectsTable projectsTable, long userId) {
        userProjectRolesDao.get().delete(projectsTable.getId(), userId);
    }

    public void removeRole(UserProjectRolesTable userProjectRolesTable) {
        userProjectRolesDao.get().delete(userProjectRolesTable.getProjectId(), userProjectRolesTable.getUserId());
        projectMembersDao.get().delete(userProjectRolesTable.getProjectId(), userProjectRolesTable.getUserId());
    }

    public void removeRole(long orgId, long userId) {
        userOrganizationRolesDao.get().delete(orgId, userId);
        organizationMembersDao.get().delete(orgId, userId);
    }

    public void addMember(long projectId, long userId) {
        projectMembersDao.get().insert(new ProjectMembersTable(projectId, userId));
    }

    public int removeMember(ProjectsTable projectsTable, long userId) {
        removeRole(projectsTable, userId);
        return projectMembersDao.get().delete(projectsTable.getId(), userId);
    }

    public int removeMember(OrganizationsTable organizationsTable, long userId) {
        removeRole(organizationsTable.getId(), userId);
        return organizationMembersDao.get().delete(organizationsTable.getId(), userId);
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
