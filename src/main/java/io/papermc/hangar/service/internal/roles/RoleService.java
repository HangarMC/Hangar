package io.papermc.hangar.service.internal.roles;

import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.internal.table.roles.IRolesDAO;
import io.papermc.hangar.db.dao.internal.table.roles.OrganizationRolesDAO;
import io.papermc.hangar.db.dao.internal.table.roles.ProjectRolesDAO;
import io.papermc.hangar.model.common.roles.OrganizationRole;
import io.papermc.hangar.model.common.roles.ProjectRole;
import io.papermc.hangar.model.common.roles.Role;
import io.papermc.hangar.model.db.roles.IRoleTable;
import io.papermc.hangar.model.db.roles.OrganizationRoleTable;
import io.papermc.hangar.model.db.roles.ProjectRoleTable;
import io.papermc.hangar.service.HangarService;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

public abstract class RoleService<T extends IRoleTable<R>, R extends Role<T>, D extends IRolesDAO<T>> extends HangarService {

    protected final D roleDao;

    protected RoleService(D roleDao) {
        this.roleDao = roleDao;
    }

    @NotNull
    public T addRole(T newRoleTable) {
        return addRole(newRoleTable, false);
    }

    @Contract("_, true -> !null")
    public T addRole(T newRoleTable, boolean ignoreIfDuplicate) {
        T existingRoleTable = roleDao.getTable(newRoleTable);
        if (existingRoleTable == null) {
            return roleDao.insert(newRoleTable);
        }
        if (!ignoreIfDuplicate) {
            throw new IllegalArgumentException("User already has a role there");
        }
        return null;
    }

    public void updateRole(T roleTable) {
        if (isUpdatable(roleTable)) {
            roleDao.update(roleTable);
        }
    }

    protected boolean isUpdatable(T roleTable) {
        return true;
    }

    public void deleteRole(T roleTable) {
        roleDao.delete(roleTable);
    }

    public T getRole(long id) {
        return roleDao.getTable(id, getHangarPrincipal().getUserId());
    }

    public T getRole(long principalId, long userId) {
        return roleDao.getTableByPrincipal(principalId, userId);
    }

    @Service
    public static class ProjectRoleService extends RoleService<ProjectRoleTable, ProjectRole, ProjectRolesDAO> {

        @Autowired
        public ProjectRoleService(HangarDao<ProjectRolesDAO> roleDao) {
            super(roleDao.get());
        }

        @Override
        public ProjectRoleTable getRole(long projectId, long userId) {
            return super.getRole(projectId, userId);
        }
    }

    @Service
    public static class OrganizationRoleService extends RoleService<OrganizationRoleTable, OrganizationRole, OrganizationRolesDAO> {

        @Autowired
        public OrganizationRoleService(HangarDao<OrganizationRolesDAO> roleDao) {
            super(roleDao.get());
        }

        @Override
        public OrganizationRoleTable getRole(long organizationId, long userId) {
            return super.getRole(organizationId, userId);
        }
    }
}
