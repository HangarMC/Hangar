package io.papermc.hangar.model.db.roles;

import io.papermc.hangar.model.common.roles.GlobalRole;
import org.jdbi.v3.core.mapper.reflect.ColumnName;

public class GlobalRoleTable implements IRoleTable {

    private final long userId;
    private final GlobalRole role;

    public GlobalRoleTable(final long userId, @ColumnName("role_id") final GlobalRole role) {
        this.userId = userId;
        this.role = role;
    }

    @Override
    public long getUserId() {
        return this.userId;
    }

    public GlobalRole getRole() {
        return this.role;
    }

    public long getRoleId() {
        return this.role.getRoleId();
    }

    @Override
    public boolean isAccepted() {
        return true;
    }

    @Override
    public void setAccepted(final boolean accepted) {
        throw new UnsupportedOperationException("Cannot change the acceptance of global roles");
    }

    @Override
    public String toString() {
        return "GlobalRoleTable{" +
            "userId=" + this.userId +
            ", role=" + this.role +
            '}';
    }
}
