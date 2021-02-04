package io.papermc.hangar.service.internal.roles;

import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.internal.table.roles.GlobalRoleDAO;
import io.papermc.hangar.model.db.roles.GlobalRoleTable;
import io.papermc.hangar.model.roles.GlobalRole;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GlobalRoleService extends RoleService<GlobalRoleTable, GlobalRole, GlobalRoleDAO> {

    public GlobalRoleService(HangarDao<GlobalRoleDAO> roleDao) {
        super(roleDao.get());
    }

    public List<GlobalRole> getGlobalRoles(long userId) {
        return roleDao.getGlobalRoleTables(userId).stream().map(GlobalRoleTable::getRole).collect(Collectors.toList());
    }

    public void removeAllGlobalRoles(long userId) {
        roleDao.deleteAll(userId);
    }
}
