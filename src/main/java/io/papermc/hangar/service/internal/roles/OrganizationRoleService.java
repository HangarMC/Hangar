package io.papermc.hangar.service.internal.roles;

import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.internal.table.roles.OrganizationRoleDAO;
import io.papermc.hangar.model.db.roles.OrganizationRoleTable;
import io.papermc.hangar.model.roles.OrganizationRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrganizationRoleService extends RoleService<OrganizationRoleTable, OrganizationRole, OrganizationRoleDAO> {

    @Autowired
    public OrganizationRoleService(HangarDao<OrganizationRoleDAO> roleDao) {
        super(roleDao.get());
    }
}
