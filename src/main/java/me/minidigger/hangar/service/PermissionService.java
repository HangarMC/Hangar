package me.minidigger.hangar.service;

import me.minidigger.hangar.db.dao.HangarDao;
import me.minidigger.hangar.db.dao.PermissionsDao;
import me.minidigger.hangar.db.model.UsersTable;
import me.minidigger.hangar.model.Permission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

@Service
public class PermissionService {

    private final HangarDao<PermissionsDao> permissionsDao;

    public static final Permission DEFAULT_GLOBAL_PERMISSIONS = Permission.ViewPublicInfo.add(Permission.EditOwnUserSettings).add(Permission.EditApiKeys);

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
        return getProjectPermissions(usersTable, pluginId, true);
    }

    public Permission getProjectPermissions(UsersTable usersTable, String pluginId, boolean includeGlobal) {
        if (usersTable == null) {
            if (includeGlobal) return DEFAULT_GLOBAL_PERMISSIONS;
            else return Permission.None;
        }
        if (includeGlobal) return addDefaults(permissionsDao.get().getProjectPermission(usersTable.getId(), pluginId));
        return permissionsDao.get().getProjectPermission(usersTable.getId(), pluginId);
    }

    public Permission getOrganizationPermissions(UsersTable usersTable, String orgName) {
        return getOrganizationPermissions(usersTable, orgName, true);
    }

    public Permission getOrganizationPermissions(UsersTable usersTable, String orgName, boolean includeGlobal) {
        if (usersTable == null) {
            if (includeGlobal) return DEFAULT_GLOBAL_PERMISSIONS;
            else return Permission.None;
        }
        if (includeGlobal) return addDefaults(permissionsDao.get().getOrgPermission(usersTable.getId(), orgName));
        else return permissionsDao.get().getOrgPermission(usersTable.getId(), orgName);
    }

    public Permission getPossibleProjectPermissions(long userId) {
        return addDefaults(permissionsDao.get().getPossibleProjectPermissions(userId));
    }

    public Permission getPossibleOrganizationPermissions(long userId) {
        return addDefaults(permissionsDao.get().getPossibleOrganizationPermissions(userId));
    }

    private Permission addDefaults(@Nullable Permission permission) {
        if (permission == null) {
            return DEFAULT_GLOBAL_PERMISSIONS;
        } else {
            return permission.add(DEFAULT_GLOBAL_PERMISSIONS);
        }
    }
}
