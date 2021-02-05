package io.papermc.hangar.model.db.roles;

import io.papermc.hangar.model.common.roles.Role;

public interface RoleTable<R extends Role<? extends RoleTable<R>>> {

    long getUserId();

    R getRole();

    long getRoleId();
}
