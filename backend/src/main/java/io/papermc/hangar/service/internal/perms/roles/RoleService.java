package io.papermc.hangar.service.internal.perms.roles;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.db.dao.internal.table.roles.IRolesDAO;
import io.papermc.hangar.model.common.roles.Role;
import io.papermc.hangar.model.db.roles.IRoleTable;
import java.util.List;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.transaction.annotation.Transactional;

public abstract class RoleService<RT extends IRoleTable<R>, R extends Role<RT>, D extends IRolesDAO<RT>> extends HangarComponent {

    protected final D roleDao;

    protected RoleService(final D roleDao) {
        this.roleDao = roleDao;
    }

    public @NotNull RT addRole(final RT newRoleTable) {
        return this.addRole(newRoleTable, false);
    }

    @Contract("_, false -> !null")
    @Transactional
    public RT addRole(final RT newRoleTable, final boolean ignoreIfDuplicate) {
        final RT existingRoleTable = this.roleDao.getTable(newRoleTable);
        if (existingRoleTable == null) {
            return this.roleDao.insert(newRoleTable);
        }
        if (!ignoreIfDuplicate) {
            throw new IllegalArgumentException("User already has a role there");
        }
        return null;
    }

    public RT changeAcceptance(RT roleTable, final boolean isAccepted) {
        if (roleTable.isAccepted() != isAccepted) {
            roleTable.setAccepted(isAccepted);
            roleTable = this.roleDao.update(roleTable);
        }
        return roleTable;
    }

    public void updateRole(final RT roleTable) {
        this.roleDao.update(roleTable);
    }

    public void deleteRole(final RT roleTable) {
        this.roleDao.delete(roleTable);
    }

    public RT getRole(final long id) {
        return this.roleDao.getTable(id);
    }

    public RT getRole(final long principalId, final long userId) {
        return this.roleDao.getTableByPrincipal(principalId, userId);
    }

    public List<RT> getRoles(final long principalId, final R role) {
        return this.roleDao.getRoleTablesByPrincipal(principalId, role.getValue());
    }
}
