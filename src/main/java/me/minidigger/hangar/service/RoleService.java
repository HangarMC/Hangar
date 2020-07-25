package me.minidigger.hangar.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import me.minidigger.hangar.controller.generated.ProjectsApiController;
import me.minidigger.hangar.db.dao.HangarDao;
import me.minidigger.hangar.db.dao.RoleDao;
import me.minidigger.hangar.db.model.RolesTable;
import me.minidigger.hangar.model.Role;

@Service
public class RoleService {

    private static final Logger log = LoggerFactory.getLogger(RoleService.class);

    private final HangarDao<RoleDao> roleDao;

    @Autowired
    public RoleService(HangarDao<RoleDao> roleDao) {
        this.roleDao = roleDao;
        init();
    }

    public void init() {
        RolesTable admin = roleDao.get().getById(1);
        if(admin != null && admin.getRole().equals(Role.HANGAR_ADMIN)) {
            log.info("Skipping role init");
            return;
        }

        log.info("Initializing roles (first start only)");
        for (Role role : Role.values()) {
            roleDao.get().insert(RolesTable.fromRole(role));
        }
    }

//    public void addRole() {
//        boolean exists =
//    }
}
