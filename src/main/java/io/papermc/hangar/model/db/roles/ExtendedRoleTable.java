package io.papermc.hangar.model.db.roles;

import io.papermc.hangar.model.db.Table;
import io.papermc.hangar.model.roles.Role;

import java.time.OffsetDateTime;

public abstract class ExtendedRoleTable<R extends Role<? extends RoleTable<R>>> extends Table implements RoleTable<R> {

    protected final long userId;
    protected final R role;
    protected boolean accepted;

    protected ExtendedRoleTable(OffsetDateTime createdAt, long id, long userId, R role, boolean accepted) {
        super(createdAt, id);
        this.userId = userId;
        this.role = role;
        this.accepted = accepted;
    }

    protected ExtendedRoleTable(long userId, R role, boolean accepted) {
        this.userId = userId;
        this.role = role;
        this.accepted = accepted;
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
        return accepted;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }

    public abstract long getPrincipalId();

    @Override
    public String toString() {
        return "ExtendedRoleTable{" +
                "userId=" + userId +
                ", role=" + role +
                ", accepted=" + accepted +
                "} " + super.toString();
    }
}
