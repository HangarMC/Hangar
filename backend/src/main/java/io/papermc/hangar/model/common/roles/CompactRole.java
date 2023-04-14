package io.papermc.hangar.model.common.roles;

import io.papermc.hangar.db.customtypes.RoleCategory;
import io.papermc.hangar.model.common.Color;
import org.jetbrains.annotations.Nullable;

public record CompactRole(String title, Color color, @Nullable Integer rank, RoleCategory category) {

    public static @Nullable CompactRole ofNullable(final @Nullable Role<?> role) {
        return role == null ? null : new CompactRole(role.getTitle(), role.getColor(), role.rank(), role.getRoleCategory());
    }
}
