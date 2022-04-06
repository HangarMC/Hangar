package io.papermc.hangar.service.internal.perms.roles;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.db.dao.internal.table.roles.IRolesDAO;
import io.papermc.hangar.model.common.roles.Role;
import io.papermc.hangar.model.db.roles.IRoleTable;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public abstract class RoleService<RT extends IRoleTable<R>, R extends Role<RT>, D extends IRolesDAO<RT>> extends HangarComponent {

    protected final D roleDao;

    protected RoleService(D roleDao) {
        this.roleDao = roleDao;
    }

    @NotNull
    public RT addRole(RT newRoleTable) {
        return addRole(newRoleTable, false);
    }

    @Contract("_, false -> !null")
    public RT addRole(RT newRoleTable, boolean ignoreIfDuplicate) {
        RT existingRoleTable = roleDao.getTable(newRoleTable);
        if (existingRoleTable == null) {
            return roleDao.insert(newRoleTable);
        }
        if (!ignoreIfDuplicate) {
            throw new IllegalArgumentException("User already has a role there");
        }
        return null;
    }

    public RT changeAcceptance(RT roleTable, boolean isAccepted) {
        if (roleTable.isAccepted() != isAccepted) {
            roleTable.setAccepted(isAccepted);
            roleTable = roleDao.update(roleTable);
        }
        return roleTable;
    }

    public void updateRole(RT roleTable) {
        roleDao.update(roleTable);
    }

    public void deleteRole(RT roleTable) {
        roleDao.delete(roleTable);
    }

    public RT getRole(long id) {
        return roleDao.getTable(id, getHangarPrincipal().getUserId());
    }

    public RT getRole(long principalId, long userId) {
        return roleDao.getTableByPrincipal(principalId, userId);
    }

}
