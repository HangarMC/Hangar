package io.papermc.hangar.model.viewhelpers;

import io.papermc.hangar.db.model.ApiKeysTable;
import io.papermc.hangar.model.NamedPermission;
import io.papermc.hangar.model.Permission;

import java.util.Collection;

public class ApiKey extends ApiKeysTable {

    public boolean getIsSubKey(Permission perm) {
        return this.getRawKeyPermissions().has(perm);
    }

    public Collection<NamedPermission> getNamedRawPermissions() {
        return this.getRawKeyPermissions().toNamed();
    }
}
