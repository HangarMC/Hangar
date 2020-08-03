package me.minidigger.hangar.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import me.minidigger.hangar.db.dao.HangarDao;
import me.minidigger.hangar.db.dao.PermissionsDao;
import me.minidigger.hangar.model.Permission;

@Service
public class PermissionService {

    private final HangarDao<PermissionsDao> permissionsDao;

    @Autowired
    public PermissionService(HangarDao<PermissionsDao> permissionsDao) {
        this.permissionsDao = permissionsDao;
    }

    public Permission getGlobalPermissions(long userid) {
        return permissionsDao.get().getGlobalPermission(userid, null);
    }

    public Permission getGlobalPermissions(String userName) {
        return permissionsDao.get().getGlobalPermission(null, userName);
    }

    public Permission getProjectPermissions(long userId, long projectId) {
        return permissionsDao.get().getProjectPermission(userId, projectId);
    }

    public Permission getPossibleProjectPermissions(long userId) {
        return permissionsDao.get().getPossibleProjectPermissions(userId);
    }

    public Permission getPossibleOrganizationPermissions(long userId) {
        return permissionsDao.get().getPossibleOrganizationPermissions(userId);
    }
}
