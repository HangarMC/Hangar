package io.papermc.hangar.model.common.roles;

import io.papermc.hangar.db.customtypes.RoleCategory;
import io.papermc.hangar.model.common.Permission;
import org.jetbrains.annotations.Nullable;

public record RoleData(String value,
                       long roleId,
                       Permission permissions,
                       RoleCategory roleCategory,
                       String title,
                       String color,
                       @Nullable Integer rank,
                       boolean assignable) {
}
