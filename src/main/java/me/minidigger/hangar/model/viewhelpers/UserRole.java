package me.minidigger.hangar.model.viewhelpers;

import me.minidigger.hangar.model.Role;

public class UserRole<R> {

    R userRole;
    private boolean isAccepted;
    private Role role;

    public UserRole(R userRole, boolean isAccepted, String roleType) {
        this.userRole = userRole;
        this.isAccepted = isAccepted;
        this.role = Role.valueOf(roleType.toUpperCase());
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
