package io.papermc.hangar.service.internal.roles;

import io.papermc.hangar.db.dao.internal.table.roles.IRolesDAO;
import io.papermc.hangar.model.common.roles.Role;
import io.papermc.hangar.model.db.roles.IRoleTable;
import io.papermc.hangar.service.HangarService;

public abstract class RoleService<T extends IRoleTable<R>, R extends Role<T>, D extends IRolesDAO<T>> extends HangarService {

    protected final D roleDao;

    protected RoleService(D roleDao) {
        this.roleDao = roleDao;
    }

    public T addRole(T newRoleTable) {
        T existingRoleTable = roleDao.getTable(newRoleTable);
        if (existingRoleTable == null) {
            return roleDao.insert(newRoleTable);
        }
        throw new IllegalArgumentException("User already has a role there");
    }

    public void updateRole(T roleTable) {
        roleDao.update(roleTable);
    }

    public void deleteRole(T roleTable) {
        roleDao.delete(roleTable);
    }

    public T getRole(long id) {
        return roleDao.getTable(id);
    }
}
