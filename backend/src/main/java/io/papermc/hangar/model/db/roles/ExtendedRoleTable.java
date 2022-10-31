package io.papermc.hangar.model.db.roles;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jdbi.v3.core.annotation.JdbiProperty;
import io.papermc.hangar.model.common.roles.Role;
import io.papermc.hangar.model.db.Table;
import io.papermc.hangar.model.internal.logs.contexts.LogContext;
import io.papermc.hangar.model.loggable.Loggable;

import java.time.OffsetDateTime;

public abstract class ExtendedRoleTable<R extends Role<? extends IRoleTable<R>>, LC extends LogContext<?, LC>> extends Table implements IRoleTable<R>, Loggable<LC> {

    protected final long userId;
    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    protected R role;
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
    @JsonIgnore
    public R getRole() {
        return role;
    }

    @Override
    public void setRole(R role) {
        this.role = role;
    }

    @Override
    @JsonIgnore
    public long getRoleId() {
        return role.getRoleId();
    }

    @Override
    @JsonIgnore
    public String getRoleType() {
        return role.getValue();
    }

    @Override
    public boolean isAccepted() {
        return accepted;
    }

    @Override
    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }

    @JdbiProperty(map=false)
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
