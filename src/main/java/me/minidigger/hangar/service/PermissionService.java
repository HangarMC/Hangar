package me.minidigger.hangar.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import me.minidigger.hangar.db.dao.HangarDao;
import me.minidigger.hangar.db.dao.PermissionsDao;
import me.minidigger.hangar.model.Permission;

@Service
public class PermissionService {

    private final HangarDao<PermissionsDao> permissionsDao;
    private final Permission ALWAYS_HAVE = Permission.ViewPublicInfo.add(Permission.EditOwnUserSettings).add(Permission.EditApiKeys);

    @Autowired
    public PermissionService(HangarDao<PermissionsDao> permissionsDao) {
        this.permissionsDao = permissionsDao;
    }

    public Permission getGlobalPermissions(long userid) {
        return permissionsDao.get().getGlobalPermission(userid, null).add(ALWAYS_HAVE);
    }

    public Permission getGlobalPermissions(String userName) {
        return permissionsDao.get().getGlobalPermission(null, userName).add(ALWAYS_HAVE);
    }

    public Permission getProjectPermissions(long userId, long projectId) {
        return permissionsDao.get().getProjectPermission(userId, projectId).add(ALWAYS_HAVE);
    }

    public Permission getProjectPermissions(long userId, String pluginId) {
        return permissionsDao.get().getProjectPermission(userId, pluginId).add(ALWAYS_HAVE);
    }

    public Permission getOrganizationPermissions(long userId, String orgName) {
        return permissionsDao.get().getOrgPermission(userId, orgName).add(ALWAYS_HAVE);
    }

    public Permission getPossibleProjectPermissions(long userId) {
        return permissionsDao.get().getPossibleProjectPermissions(userId).add(ALWAYS_HAVE);
    }

    public Permission getPossibleOrganizationPermissions(long userId) {
        return permissionsDao.get().getPossibleOrganizationPermissions(userId).add(ALWAYS_HAVE);
    }
}
