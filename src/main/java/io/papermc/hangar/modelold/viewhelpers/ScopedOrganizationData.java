package io.papermc.hangar.modelold.viewhelpers;

import io.papermc.hangar.modelold.Permission;

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
