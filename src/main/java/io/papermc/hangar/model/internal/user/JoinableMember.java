package io.papermc.hangar.model.internal.user;

import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.model.db.roles.ExtendedRoleTable;
import org.jdbi.v3.core.mapper.Nested;

public class JoinableMember<R extends ExtendedRoleTable<?>> {

    private final R role;
    private final UserTable user;

    public JoinableMember(@Nested R role, UserTable user) {
        this.role = role;
        this.user = user;
    }

    public R getRole() {
        return role;
    }

    public UserTable getUser() {
        return user;
    }

    @Override
    public String toString() {
        return "JoinableMember{" +
                "role=" + role +
                ", user=" + user +
                '}';
    }
}
