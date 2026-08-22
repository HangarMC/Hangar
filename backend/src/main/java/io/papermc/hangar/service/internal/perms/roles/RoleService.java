package io.papermc.hangar.service.internal.perms.roles;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.db.dao.internal.table.roles.IMemberRolesDAO;
import io.papermc.hangar.model.db.roles.ExtendedRoleTable;
import java.util.List;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;
import org.springframework.transaction.annotation.Transactional;

public abstract class RoleService<RT extends ExtendedRoleTable<?>, D extends IMemberRolesDAO<RT>> extends HangarComponent {

    protected final D roleDao;

    protected RoleService(final D roleDao) {
        this.roleDao = roleDao;
    }

    public RT addRole(final RT newRoleTable) {
        return this.addRole(newRoleTable, false);
    }

    @Contract("_, false -> !null")
    @Transactional
    public RT addRole(final RT newRoleTable, final boolean ignoreIfDuplicate) {
        final RT existingRoleTable = this.roleDao.getTable(newRoleTable);
        if (existingRoleTable == null) {
            // the insert only hands back the generated id, so read the row to get a table carrying it
            return this.roleDao.getTable(this.roleDao.insert(newRoleTable));
        }
        if (!ignoreIfDuplicate) {
            throw new IllegalArgumentException("User already has a role there");
        }
        return null;
    }

    public RT changeAcceptance(final RT roleTable, final boolean isAccepted) {
        if (roleTable.isAccepted() != isAccepted) {
            roleTable.setAccepted(isAccepted);
            this.roleDao.update(roleTable);
        }
        return roleTable;
    }

    public void updateRole(final RT roleTable) {
        this.roleDao.update(roleTable);
    }

    public void deleteRole(final RT roleTable) {
        this.roleDao.delete(roleTable);
    }

    public @Nullable RT getRole(final long id) {
        return this.roleDao.getTable(id);
    }

    public @Nullable RT getRole(final long principalId, final long userId) {
        return this.roleDao.getTableByPrincipal(principalId, userId);
    }

    public List<RT> getOwnerRoles(final long principalId) {
        return this.roleDao.getOwnerTables(principalId);
    }
}
