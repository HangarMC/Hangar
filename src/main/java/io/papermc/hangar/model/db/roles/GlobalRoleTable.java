package io.papermc.hangar.model.db.roles;

import io.papermc.hangar.model.roles.GlobalRole;
import org.jdbi.v3.core.mapper.reflect.ColumnName;

public class GlobalRoleTable implements RoleTable<GlobalRole> {

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
    public long getRoleId() {
        return role.getRoleId();
    }

    @Override
    public String toString() {
        return "GlobalRoleTable{" +
                "userId=" + userId +
                ", role=" + role +
                '}';
    }
}
