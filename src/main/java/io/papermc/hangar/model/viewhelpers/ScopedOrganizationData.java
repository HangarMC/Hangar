package io.papermc.hangar.model.viewhelpers;

import io.papermc.hangar.model.Permission;

public class ScopedOrganizationData {
    private final Permission permissions;

    public ScopedOrganizationData(Permission permissions) {
        this.permissions = permissions;
    }

    public ScopedOrganizationData() {
        this.permissions = Permission.None;
    }

    public Permission getPermissions() {
        return permissions;
    }
}
