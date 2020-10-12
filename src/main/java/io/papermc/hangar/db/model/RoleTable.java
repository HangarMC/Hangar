package io.papermc.hangar.db.model;

import io.papermc.hangar.model.Role;

public interface RoleTable {

    long getId();

    boolean getIsAccepted();

    void setIsAccepted(boolean isAccepted);

    String getRoleType();

    void setRoleType(String roleType);

    Role getRole();
}
