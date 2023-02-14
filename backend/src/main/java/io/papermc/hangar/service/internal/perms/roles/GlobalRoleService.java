package io.papermc.hangar.service.internal.perms.roles;

import io.papermc.hangar.db.dao.internal.table.roles.GlobalRolesDAO;
import io.papermc.hangar.model.common.roles.GlobalRole;
import io.papermc.hangar.model.db.roles.GlobalRoleTable;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GlobalRoleService extends RoleService<GlobalRoleTable, GlobalRole, GlobalRolesDAO> {

    @Autowired
    public GlobalRoleService(final GlobalRolesDAO roleDao) {
        super(roleDao);
    }

    public List<GlobalRole> getGlobalRoles(final long userId) {
        return this.roleDao.getGlobalRoleTables(userId).stream().map(GlobalRoleTable::getRole).collect(Collectors.toList());
    }

    public void removeAllGlobalRoles(final long userId) {
        this.roleDao.deleteAll(userId);
    }
}
