package io.papermc.hangar.service.internal.roles;

import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.internal.table.roles.ProjectRoleDAO;
import io.papermc.hangar.model.db.roles.ProjectRoleTable;
import io.papermc.hangar.model.roles.ProjectRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProjectRoleService extends RoleService<ProjectRoleTable, ProjectRole, ProjectRoleDAO> {

    @Autowired
    public ProjectRoleService(HangarDao<ProjectRoleDAO> roleDao) {
        super(roleDao.get());
    }
}
