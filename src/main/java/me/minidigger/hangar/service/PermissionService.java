package me.minidigger.hangar.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import me.minidigger.hangar.db.dao.HangarDao;
import me.minidigger.hangar.db.dao.PermissionsDao;
import me.minidigger.hangar.model.NamedPermission;
import me.minidigger.hangar.model.Permission;

@Service
public class PermissionService {

    private final HangarDao<PermissionsDao> permissionsDao;

    @Autowired
    public PermissionService(HangarDao<PermissionsDao> permissionsDao) {
        this.permissionsDao = permissionsDao;
    }

    public List<NamedPermission> getGlobalPermissions(long userid) {
        return permissionsDao.get().getGlobalPermission(userid, null).toNamed();
    }

    public List<NamedPermission> getGlobalPermissions(String userName) {
        return permissionsDao.get().getGlobalPermission(null, userName).toNamed();
    }

    public List<NamedPermission> getProjectPermissions(long userId, long projectId) {
        return permissionsDao.get().getProjectPermission(userId, projectId).toNamed();
    }

    public Permission getPossibleProjectPermissions(long userId) {
        return permissionsDao.get().getPossibleProjectPermissions(userId);
    }

    public Permission getPossibleOrganizationPermissions(long userId) {
        return permissionsDao.get().getPossibleOrganizationPermissions(userId);
    }
}
