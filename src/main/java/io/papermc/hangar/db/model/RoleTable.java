package io.papermc.hangar.db.model;

import io.papermc.hangar.model.Role;

public interface RoleTable {

    boolean getIsAccepted();

    String getRoleType();

    Role getRole();
}
