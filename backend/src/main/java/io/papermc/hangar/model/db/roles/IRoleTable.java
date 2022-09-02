package io.papermc.hangar.model.db.roles;

import io.papermc.hangar.model.common.roles.Role;

public interface IRoleTable<R extends Role<? extends IRoleTable<R>>> {

    long getUserId();

    R getRole();

    void setRole(R role);

    long getRoleId();

    String getRoleType();

    boolean isAccepted();

    void setAccepted(boolean accepted);
}
