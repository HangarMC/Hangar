package me.minidigger.hangar.service;

import me.minidigger.hangar.db.dao.HangarDao;
import me.minidigger.hangar.db.dao.PermissionsDao;
import me.minidigger.hangar.model.Permission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PermissionService {

    private final HangarDao<PermissionsDao> permissionsDao;

    @Autowired
    public PermissionService(HangarDao<PermissionsDao> permissionsDao) {
        this.permissionsDao = permissionsDao;
    }

    public Permission getGlobalPermission(long userId) {
        return permissionsDao.get().getGlobalPermission(userId, null);
    }

    public Permission getGlobalPermission(String userName) {
        return permissionsDao.get().getGlobalPermission(null, userName);
    }
}
