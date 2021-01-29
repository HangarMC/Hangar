package io.papermc.hangar.model.internal;

import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.model.db.roles.ExtendedRoleTable;

public class JoinableMember<R extends ExtendedRoleTable> {

    private final R role;
    private final UserTable user;

    public JoinableMember(R role, UserTable user) {
        this.role = role;
        this.user = user;
    }

    public R getRole() {
        return role;
    }

    public UserTable getUser() {
        return user;
    }
}
