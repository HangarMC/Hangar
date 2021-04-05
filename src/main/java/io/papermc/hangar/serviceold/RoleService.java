package io.papermc.hangar.serviceold;

import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.daoold.OrganizationMembersDao;
import io.papermc.hangar.db.daoold.ProjectMembersDao;
import io.papermc.hangar.db.daoold.UserGlobalRolesDao;
import io.papermc.hangar.db.daoold.UserOrganizationRolesDao;
import io.papermc.hangar.db.daoold.UserProjectRolesDao;
import io.papermc.hangar.db.modelold.RoleTable;
import io.papermc.hangar.db.modelold.UserOrganizationRolesTable;
import io.papermc.hangar.db.modelold.UserProjectRolesTable;
import io.papermc.hangar.modelold.Role;
import org.postgresql.shaded.com.ongres.scram.common.util.Preconditions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service("oldRoleService")
@Deprecated(forRemoval = true)
public class RoleService {

    private final HangarDao<UserProjectRolesDao> userProjectRolesDao;
    private final HangarDao<ProjectMembersDao> projectMembersDao;
    private final HangarDao<UserGlobalRolesDao> userGlobalRolesDao;
    private final HangarDao<UserOrganizationRolesDao> userOrganizationRolesDao;
    private final HangarDao<OrganizationMembersDao> organizationMembersDao;

    @Autowired
    public RoleService(HangarDao<UserProjectRolesDao> userProjectRolesDao, HangarDao<ProjectMembersDao> projectMembersDao, HangarDao<UserGlobalRolesDao> userGlobalRolesDao, HangarDao<UserOrganizationRolesDao> userOrganizationRolesDao, HangarDao<OrganizationMembersDao> organizationMembersDao) {
        this.userProjectRolesDao = userProjectRolesDao;
        this.projectMembersDao = projectMembersDao;
        this.userGlobalRolesDao = userGlobalRolesDao;
        this.userOrganizationRolesDao = userOrganizationRolesDao;
        this.organizationMembersDao = organizationMembersDao;
    }

    public UserProjectRolesTable getUserProjectRole(long id) {
        return userProjectRolesDao.get().getById(id);
    }

    public UserOrganizationRolesTable getUserOrgRole(long id) {
        return userOrganizationRolesDao.get().getById(id);
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

    public void updateRole(UserProjectRolesTable userProjectRolesTable) {
        userProjectRolesDao.get().update(userProjectRolesTable);
    }

    public void updateRole(UserOrganizationRolesTable userOrganizationRolesTable) {
        userOrganizationRolesDao.get().update(userOrganizationRolesTable);
    }

    public void removeRole(UserProjectRolesTable userProjectRolesTable) {
        userProjectRolesDao.get().delete(userProjectRolesTable.getProjectId(), userProjectRolesTable.getUserId());
        projectMembersDao.get().delete(userProjectRolesTable.getProjectId(), userProjectRolesTable.getUserId());
    }

    public void removeRole(long orgId, long userId) {
        userOrganizationRolesDao.get().delete(orgId, userId);
        organizationMembersDao.get().delete(orgId, userId);
    }


    public List<Role> getGlobalRolesForUser(@Nullable Long userId, @Nullable String userName) {
        Preconditions.checkArgument(userId != null || userName != null, "One of (userId, userName) must be nonnull");
        return userGlobalRolesDao.get().getRolesByUserId(userId, userName).stream().map(adf -> Role.fromId(adf.getId())).collect(Collectors.toList());
    }
}
