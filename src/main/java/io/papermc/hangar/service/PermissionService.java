package io.papermc.hangar.service;

import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.PermissionsDao;
import io.papermc.hangar.db.model.UsersTable;
import io.papermc.hangar.model.Permission;
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

    public Permission getProjectPermissions(long userId, String author, String slug) {
        return addDefaults(permissionsDao.get().getProjectPermission(userId, author, slug));
    }

    public Permission getProjectPermissions(long userId, String pluginId) {
        return addDefaults(permissionsDao.get().getProjectPermission(userId, pluginId));
    }

    public Permission getProjectPermissions(UsersTable usersTable, String pluginId) {
        if (usersTable == null) {
            return Permission.None;
        }
        return addDefaults(permissionsDao.get().getProjectPermission(usersTable.getId(), pluginId));
    }

    public Permission getOrganizationPermissions(long userId, String orgName) {
        return addDefaults(permissionsDao.get().getOrgPermission(userId, orgName));
    }

    public Permission getOrganizationPermissions(UsersTable usersTable, String orgName) {
        if (usersTable == null) {
                return Permission.None;
        }
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
            return DEFAULT_GLOBAL_PERMISSIONS;
        } else {
            return permission.add(DEFAULT_GLOBAL_PERMISSIONS);
        }
    }
}
