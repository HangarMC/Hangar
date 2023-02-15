package io.papermc.hangar.model.api.permissions;

import io.papermc.hangar.model.common.NamedPermission;
import io.papermc.hangar.model.common.PermissionType;
import java.util.List;

public record UserPermissions(PermissionType type, String permissionBinString, List<NamedPermission> permissions) {
}
