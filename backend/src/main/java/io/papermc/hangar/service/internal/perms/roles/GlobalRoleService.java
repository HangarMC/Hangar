package io.papermc.hangar.service.internal.perms.roles;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.db.dao.internal.table.roles.GlobalRolesDAO;
import io.papermc.hangar.model.common.roles.GlobalRole;
import io.papermc.hangar.model.db.roles.GlobalRoleTable;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GlobalRoleService extends HangarComponent {

    private final GlobalRolesDAO roleDao;

    @Autowired
    public GlobalRoleService(final GlobalRolesDAO roleDao) {
        this.roleDao = roleDao;
    }

    public void addRole(final GlobalRoleTable table) {
        this.roleDao.insert(table);
    }

    public void deleteRole(final GlobalRoleTable table) {
        this.roleDao.delete(table);
    }

    public void deleteAllRoles(final long userId) {
        this.roleDao.deleteAll(userId);
    }

    public List<GlobalRole> getGlobalRoles(final long userId) {
        return this.roleDao.getGlobalRoleTables(userId).stream().map(GlobalRoleTable::getRole).toList();
    }
}
