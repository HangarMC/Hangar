package io.papermc.hangar.db.modelold;

import io.papermc.hangar.modelold.Role;

public interface RoleTable {

    long getId();

    boolean isAccepted();

    void setAccepted(boolean isAccepted);

    String getRoleType();

    void setRoleType(String roleType);

    Role getRole();
}
