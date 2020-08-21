package io.papermc.hangar.model.viewhelpers;

import io.papermc.hangar.db.model.RoleTable;
import io.papermc.hangar.model.Role;

public class UserRole<R extends RoleTable> {

    private final R userRole;
    private final boolean isAccepted;
    private final Role role;

    public UserRole(R userRole) {
        this.userRole = userRole;
        this.isAccepted = userRole.getIsAccepted();
        this.role = Role.valueOf(userRole.getRoleType().toUpperCase());
    }

    public R getUserRole() {
        return userRole;
    }

    public boolean isAccepted() {
        return isAccepted;
    }

    public Role getRole() {
        return role;
    }
}
