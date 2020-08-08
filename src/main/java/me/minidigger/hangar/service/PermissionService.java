package me.minidigger.hangar.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import me.minidigger.hangar.db.dao.HangarDao;
import me.minidigger.hangar.db.dao.PermissionsDao;
import me.minidigger.hangar.model.Permission;

@Service
public class PermissionService {

    private final HangarDao<PermissionsDao> permissionsDao;

    public static final Permission DEFAULT_GLOBAL_PERMISSIONS = Permission.ViewPublicInfo.add(Permission.EditOwnUserSettings).add(Permission.EditApiKeys);

    @Autowired
    public PermissionService(HangarDao<PermissionsDao> permissionsDao) {
        this.permissionsDao = permissionsDao;
    }

    public Permission getGlobalPermissions(long userid) {
        return orDefault(permissionsDao.get().getGlobalPermission(userid, null));
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

    private Permission orDefault(@Nullable Permission permission) {
        return permission == null ? DEFAULT_GLOBAL_PERMISSIONS : permission.add(DEFAULT_GLOBAL_PERMISSIONS);
    }
}
