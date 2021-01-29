package io.papermc.hangar.model.db.roles;

import io.papermc.hangar.model.db.Table;
import io.papermc.hangar.model.roles.Role;
import org.jdbi.v3.core.enums.EnumByOrdinal;

import java.time.OffsetDateTime;

public abstract class ExtendedRoleTable<R extends Role<? extends RoleTable<R>>> extends Table implements RoleTable<R> {

    protected final long userId;
    @EnumByOrdinal
    protected final R role;
    protected final boolean isAccepted;

    public ExtendedRoleTable(OffsetDateTime createdAt, long id, long userId, R role, boolean isAccepted) {
        super(createdAt, id);
        this.userId = userId;
        this.role = role;
        this.isAccepted = isAccepted;
    }

    public ExtendedRoleTable(long userId, R role, boolean isAccepted) {
        this.userId = userId;
        this.role = role;
        this.isAccepted = isAccepted;
    }

    @Override
    public long getUserId() {
        return userId;
    }

    @Override
    public R getRole() {
        return role;
    }

    @Override
    public long getRoleId() {
        return role.getRoleId();
    }

    public boolean isAccepted() {
        return isAccepted;
    }

    @Override
    public String toString() {
        return "ExtendedRoleTable{" +
                "userId=" + userId +
                ", role=" + role +
                ", isAccepted=" + isAccepted +
                "} " + super.toString();
    }
}
