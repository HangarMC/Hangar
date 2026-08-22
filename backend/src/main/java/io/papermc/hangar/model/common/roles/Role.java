package io.papermc.hangar.model.common.roles;

import io.papermc.hangar.db.customtypes.RoleCategory;
import io.papermc.hangar.model.common.Color;
import io.papermc.hangar.model.common.Permission;
import java.util.HashMap;
import java.util.Map;
import org.jspecify.annotations.Nullable;

// Global only; project and organization membership carries its own permissions, see MemberPermissions
public interface Role {

    Map<String, Role> VALUE_ROLES = new HashMap<>();
    Map<Long, Role> ID_ROLES = new HashMap<>();

    static <C extends Enum<C> & Role> void registerRole(final C roleEnum) {
        if (ID_ROLES.containsKey(roleEnum.getRoleId()) || VALUE_ROLES.containsKey(roleEnum.getValue())) {
            throw new IllegalArgumentException(roleEnum + " has a duplicate role ID or value");
        }
        ID_ROLES.put(roleEnum.getRoleId(), roleEnum);
        VALUE_ROLES.put(roleEnum.getValue(), roleEnum);
    }

    String getValue();

    long getRoleId();

    RoleCategory getRoleCategory();

    Permission getPermissions();

    String getTitle();

    Color getColor();

    boolean isAssignable();

    @Nullable Integer rank();
}
