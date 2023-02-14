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
        if (ID_ROLES.containsKey(roleEnum.roleId()) || VALUE_ROLES.containsKey(roleEnum.value())) {
            throw new IllegalArgumentException(roleEnum + " has a duplicate role ID or value");
        }
        ID_ROLES.put(roleEnum.roleId(), roleEnum);
        VALUE_ROLES.put(roleEnum.value(), roleEnum);
    }

    @NotNull
    String value();

    long roleId();

    @NotNull
    RoleCategory roleCategory();

    @NotNull
    Permission permissions();

    @NotNull
    String title();

    @NotNull
    Color color();

    boolean assignable();

    @Nullable Integer rank();

    @NotNull
    T create(@Nullable Long principalId, @Nullable UUID principalUuid, long userId, boolean isAccepted);
}
