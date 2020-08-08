package me.minidigger.hangar.service;

import me.minidigger.hangar.db.model.UsersTable;
import org.jetbrains.annotations.Nullable;
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
        return addDefaults(permissionsDao.get().getGlobalPermission(userid, null));
    }

    public Permission getGlobalPermissions(String userName) {
        return addDefaults(permissionsDao.get().getGlobalPermission(null, userName));
    }

    public Permission getProjectPermissions(long userId, long projectId) {
        return addDefaults(permissionsDao.get().getProjectPermission(userId, projectId));
    }

    public Permission getProjectPermissions(UsersTable usersTable, String pluginId) {
        if (usersTable == null) return ALWAYS_HAVE;
        return addDefaults(permissionsDao.get().getProjectPermission(usersTable.getId(), pluginId));
    }

    public Permission getOrganizationPermissions(UsersTable usersTable, String orgName) {
        if (usersTable == null) return ALWAYS_HAVE;
        return addDefaults(permissionsDao.get().getOrgPermission(usersTable.getId(), orgName));
    }

    public Permission getPossibleProjectPermissions(long userId) {
        return addDefaults(permissionsDao.get().getPossibleProjectPermissions(userId));
    }

    public Permission getPossibleOrganizationPermissions(long userId) {
        return addDefaults(permissionsDao.get().getPossibleOrganizationPermissions(userId));
    }

    private Permission addDefaults(@Nullable Permission permission) {
        if (permission == null) {
            return ALWAYS_HAVE;
        } else {
            return permission.add(ALWAYS_HAVE);
        }
    }
}