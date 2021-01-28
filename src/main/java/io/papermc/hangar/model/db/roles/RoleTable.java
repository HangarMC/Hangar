package io.papermc.hangar.model.db.roles;

import io.papermc.hangar.model.Role;
import io.papermc.hangar.model.db.Table;
import org.jdbi.v3.core.enums.EnumByOrdinal;

import java.time.OffsetDateTime;

public abstract class RoleTable extends Table {

    protected final long userId;
    protected final Role role;
    protected final boolean isAccepted;

    public RoleTable(OffsetDateTime createdAt, long id, long userId, Role role, boolean isAccepted) {
        super(createdAt, id);
        this.userId = userId;
        this.role = role;
        this.isAccepted = isAccepted;
    }

    public RoleTable(long userId, Role role, boolean isAccepted) {
        this.userId = userId;
        this.role = role;
        this.isAccepted = isAccepted;
    }

    public long getUserId() {
        return userId;
    }

    @EnumByOrdinal
    public Role getRole() {
        return role;
    }

    public boolean isAccepted() {
        return isAccepted;
    }
}
