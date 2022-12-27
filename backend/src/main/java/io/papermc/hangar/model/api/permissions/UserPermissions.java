package io.papermc.hangar.model.api.permissions;

import io.papermc.hangar.model.common.NamedPermission;
import io.papermc.hangar.model.common.PermissionType;

import java.util.List;

public class UserPermissions {

    private final PermissionType type;
    private final String permissionBinString;
    private final List<NamedPermission> permissions;

    public UserPermissions(final PermissionType type, final String permissionBinString, final List<NamedPermission> permissions) {
        this.type = type;
        this.permissionBinString = permissionBinString;
        this.permissions = permissions;
    }

    public PermissionType getType() {
        return this.type;
    }

    public String getPermissionBinString() {
        return this.permissionBinString;
    }

    public List<NamedPermission> getPermissions() {
        return this.permissions;
    }

    @Override
    public String toString() {
        return "UserPermissions{" +
                "type=" + this.type +
                ", permissionBinString='" + this.permissionBinString + '\'' +
                ", permissions=" + this.permissions +
                '}';
    }
}
