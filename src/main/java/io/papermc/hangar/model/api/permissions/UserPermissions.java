package io.papermc.hangar.model.api.permissions;

import io.papermc.hangar.model.PermissionType;
import io.papermc.hangar.modelold.NamedPermission;

import java.util.List;

public class UserPermissions {

    private final PermissionType type;
    private final List<NamedPermission> permissions;

    public UserPermissions(PermissionType type, List<NamedPermission> permissions) {
        this.type = type;
        this.permissions = permissions;
    }

    public PermissionType getType() {
        return type;
    }

    public List<NamedPermission> getPermissions() {
        return permissions;
    }
}
