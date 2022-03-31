package io.papermc.hangar.model.viewhelpers;

import io.papermc.hangar.db.model.UsersTable;
import io.papermc.hangar.model.Role;
import org.jdbi.v3.core.mapper.Nested;
import org.jetbrains.annotations.NotNull;

public class ProjectMember {

    @Nested("usr")
    private UsersTable user;
    private Role role;
    private boolean isAccepted = false;

    public ProjectMember(@NotNull UsersTable user, Role role, boolean isAccepted) {
        this.user = user;
        this.role = role;
        this.isAccepted = isAccepted;
    }

    public ProjectMember() { }

    @Nested("usr")
    public UsersTable getUser() {
        return user;
    }

    @Nested("usr")
    public void setUser(UsersTable user) {
        this.user = user;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean getIsAccepted() {
        return isAccepted;
    }

    public void setIsAccepted(boolean accepted) {
        isAccepted = accepted;
    }
}
