package io.papermc.hangar.model.common;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "A set of related permissions, presented together when editing a member")
public record PermissionGroup(@Schema(description = "i18n key suffix under 'permissionGroup.'") String name, List<NamedPermission> permissions) {

    public Permission asPermission() {
        return this.permissions.stream().map(NamedPermission::getPermission).reduce(Permission::add).orElse(Permission.None);
    }
}
