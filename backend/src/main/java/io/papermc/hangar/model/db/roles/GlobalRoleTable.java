package io.papermc.hangar.model.db.roles;

import io.papermc.hangar.model.common.roles.GlobalRole;
import org.jdbi.v3.core.mapper.reflect.ColumnName;

public class GlobalRoleTable implements IRoleTable<GlobalRole> {

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

    @Override
    public GlobalRole getRole() {
        return this.role;
    }

    @Override
    public void setRole(final GlobalRole role) {
        throw new UnsupportedOperationException("Delete the global role and add a new one to change it");
    }

    @Override
    public long getRoleId() {
        return this.role.getRoleId();
    }

    @Override
    public String getRoleType() {
        return this.role.getValue();
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
