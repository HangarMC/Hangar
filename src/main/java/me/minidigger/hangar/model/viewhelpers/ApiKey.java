package me.minidigger.hangar.model.viewhelpers;

import me.minidigger.hangar.db.model.ApiKeysTable;
import me.minidigger.hangar.model.NamedPermission;
import me.minidigger.hangar.model.Permission;

import java.util.Collection;

public class ApiKey extends ApiKeysTable {

    public boolean getIsSubKey(Permission perm) {
        return this.getRawKeyPermissions().has(perm);
    }

    public Collection<NamedPermission> getNamedRawPermissions() {
        return this.getRawKeyPermissions().toNamed();
    }
}
