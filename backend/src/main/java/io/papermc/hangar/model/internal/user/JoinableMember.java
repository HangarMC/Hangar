package io.papermc.hangar.model.internal.user;

import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.model.db.roles.ExtendedRoleTable;
import org.jdbi.v3.core.mapper.Nested;

public class JoinableMember<R extends ExtendedRoleTable<?, ?>> {

    private final R role;
    private final UserTable user;
    private final boolean hidden;
    private String avatarUrl;

    public JoinableMember(@Nested final R role, final UserTable user, final boolean hidden) {
        this.role = role;
        this.user = user;
        this.hidden = hidden;
    }

    public R getRole() {
        return this.role;
    }

    public UserTable getUser() {
        return this.user;
    }

    public boolean isHidden() {
        return this.hidden;
    }

    public String getAvatarUrl() {
        return this.avatarUrl;
    }

    public void setAvatarUrl(final String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    @Override
    public String toString() {
        return "JoinableMember{" +
            "role=" + this.role +
            ", user=" + this.user +
            ", hidden=" + this.hidden +
            '}';
    }
}
