package me.minidigger.hangar.model.viewhelpers;

import me.minidigger.hangar.db.model.ApiKeysTable;
import me.minidigger.hangar.model.NamedPermission;
import me.minidigger.hangar.model.Permission;

import java.util.Collection;

public class ApiKey {
    private ApiKeysTable key;

    public ApiKey(ApiKeysTable key) {
        this.key = key;
    }

    public ApiKeysTable getKey() {
        return key;
    }

    public boolean getIsSubKey(Permission perm) {
        return key.getRawKeyPermissions().has(perm);
    }

    public Collection<NamedPermission> getNamedRawPermissions() {
        return key.getRawKeyPermissions().toNamed();
    }
}
