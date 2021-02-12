package io.papermc.hangar.model.api.permissions;

import io.papermc.hangar.model.common.NamedPermission;
import io.papermc.hangar.model.common.PermissionType;

import java.util.List;

public class UserPermissions {

    private final PermissionType type;
    private final String permissionBinString;
    private final List<NamedPermission> permissions;

    public UserPermissions(PermissionType type, String permissionBinString, List<NamedPermission> permissions) {
        this.type = type;
        this.permissionBinString = permissionBinString;
        this.permissions = permissions;
    }

    public PermissionType getType() {
        return type;
    }

    public String getPermissionBinString() {
        return permissionBinString;
    }

    public List<NamedPermission> getPermissions() {
        return permissions;
    }

    @Override
    public String toString() {
        return "UserPermissions{" +
                "type=" + type +
                ", permissionBinString='" + permissionBinString + '\'' +
                ", permissions=" + permissions +
                '}';
    }
}
