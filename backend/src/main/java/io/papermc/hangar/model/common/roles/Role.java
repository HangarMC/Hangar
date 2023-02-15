package io.papermc.hangar.model.common.roles;

import io.papermc.hangar.db.customtypes.RoleCategory;
import io.papermc.hangar.model.common.Color;
import io.papermc.hangar.model.common.Permission;
import io.papermc.hangar.model.db.roles.IRoleTable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// TODO Remove this and the enums to keep everything data driven (see RoleData)
public interface Role<T extends IRoleTable<? extends Role<T>>> {

    Map<String, Role<?>> VALUE_ROLES = new HashMap<>();
    Map<Long, Role<?>> ID_ROLES = new HashMap<>();

    static <C extends Enum<C> & Role<?>> void registerRole(final C roleEnum) {
        if (ID_ROLES.containsKey(roleEnum.getRoleId()) || VALUE_ROLES.containsKey(roleEnum.getValue())) {
            throw new IllegalArgumentException(roleEnum + " has a duplicate role ID or value");
        }
        ID_ROLES.put(roleEnum.getRoleId(), roleEnum);
        VALUE_ROLES.put(roleEnum.getValue(), roleEnum);
    }

    @NotNull
    String getValue();

    long getRoleId();

    @NotNull
    RoleCategory getRoleCategory();

    @NotNull
    Permission getPermissions();

    @NotNull
    String getTitle();

    @NotNull
    Color getColor();

    boolean isAssignable();

    @Nullable Integer rank();

    @NotNull
    T create(@Nullable Long principalId, @Nullable UUID principalUuid, long userId, boolean isAccepted);
}
