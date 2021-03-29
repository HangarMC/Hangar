package io.papermc.hangar.model.db.roles;

import io.papermc.hangar.model.common.roles.GlobalRole;
import org.jdbi.v3.core.mapper.reflect.ColumnName;

public class GlobalRoleTable implements IRoleTable<GlobalRole> {

    private final long userId;
    private final GlobalRole role;

    public GlobalRoleTable(long userId, @ColumnName("role_id") GlobalRole role) {
        this.userId = userId;
        this.role = role;
    }

    @Override
    public long getUserId() {
        return userId;
    }

    @Override
    public GlobalRole getRole() {
        return role;
    }

    @Override
    public void setRole(GlobalRole role) {
        throw new UnsupportedOperationException("Delete the global role and add a new one to change it");
    }

    @Override
    public long getRoleId() {
        return role.getRoleId();
    }

    @Override
    public String getRoleType() {
        return role.getValue();
    }

    @Override
    public boolean isAccepted() {
        return true;
    }

    @Override
    public void setAccepted(boolean accepted) {
        throw new UnsupportedOperationException("Cannot change the acceptance of global roles");
    }

    @Override
    public String toString() {
        return "GlobalRoleTable{" +
                "userId=" + userId +
                ", role=" + role +
                '}';
    }
}
