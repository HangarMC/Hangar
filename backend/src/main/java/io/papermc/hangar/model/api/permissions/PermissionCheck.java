package io.papermc.hangar.model.api.permissions;

import io.papermc.hangar.model.common.PermissionType;

public record PermissionCheck(PermissionType type, boolean result) {
}
