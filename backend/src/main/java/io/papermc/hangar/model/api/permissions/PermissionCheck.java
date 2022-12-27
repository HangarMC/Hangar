package io.papermc.hangar.model.api.permissions;

import io.papermc.hangar.model.common.PermissionType;

public class PermissionCheck {

    private final PermissionType type;
    private final boolean result;

    public PermissionCheck(final PermissionType type, final boolean result) {
        this.type = type;
        this.result = result;
    }

    public PermissionType getType() {
        return this.type;
    }

    public boolean isResult() {
        return this.result;
    }
}
