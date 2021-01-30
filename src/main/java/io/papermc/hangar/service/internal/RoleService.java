package io.papermc.hangar.service.internal;

import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.internal.table.roles.GlobalRoleDAO;
import io.papermc.hangar.model.db.roles.RoleTable;
import io.papermc.hangar.model.roles.Role;
import io.papermc.hangar.service.HangarService;
import org.springframework.stereotype.Service;

@Service
public class RoleService extends HangarService {

    private final GlobalRoleDAO globalRoleDAO;

    public RoleService(HangarDao<GlobalRoleDAO> globalRoleDAO) {
        this.globalRoleDAO = globalRoleDAO.get();
    }

    public <T extends RoleTable<? extends Role<T>>> void addRole(T newRoleTable) {
        T existingRoleTable = newRoleTable.getRole().getRoleDAO().getTable(newRoleTable);
        if (existingRoleTable == null) {
            newRoleTable.getRole().getRoleDAO().insert(newRoleTable);
        } else {
            throw new IllegalArgumentException("Use already has a role there");
        }
    }

    public <T extends RoleTable<? extends Role<T>>> void deleteRole(T roleTable) {
        roleTable.getRole().getRoleDAO().delete(roleTable);
    }

    public void removeAllGlobalRoles(long userId) {
        globalRoleDAO.deleteAll(userId);
    }
}
