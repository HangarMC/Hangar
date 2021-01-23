package io.papermc.hangar.model.api.permissions;

import io.papermc.hangar.model.PermissionType;

public class PermissionCheck {

    private final PermissionType type;
    private final boolean result;

    public PermissionCheck(PermissionType type, boolean result) {
        this.type = type;
        this.result = result;
    }

    public PermissionType getType() {
        return type;
    }

    public boolean isResult() {
        return result;
    }
}
