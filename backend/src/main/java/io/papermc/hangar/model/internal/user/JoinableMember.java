package io.papermc.hangar.model.internal.user;

import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.model.db.roles.ExtendedRoleTable;
import org.jdbi.v3.core.mapper.Nested;

public class JoinableMember<R extends ExtendedRoleTable<?, ?>> {

    private final R role;
    private final UserTable user;
    private final boolean hidden;

    public JoinableMember(@Nested R role, UserTable user, boolean hidden) {
        this.role = role;
        this.user = user;
        this.hidden = hidden;
    }

    public R getRole() {
        return role;
    }

    public UserTable getUser() {
        return user;
    }

    public boolean isHidden() {
        return hidden;
    }

    @Override
    public String toString() {
        return "JoinableMember{" +
                "role=" + role +
                ", user=" + user +
                ", hidden=" + hidden +
                '}';
    }
}
