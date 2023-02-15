package io.papermc.hangar.model.db.roles;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.papermc.hangar.model.common.roles.Role;
import io.papermc.hangar.model.db.Table;
import io.papermc.hangar.model.internal.logs.contexts.LogContext;
import io.papermc.hangar.model.loggable.Loggable;
import java.time.OffsetDateTime;
import org.jdbi.v3.core.annotation.JdbiProperty;

public abstract class ExtendedRoleTable<R extends Role<? extends IRoleTable<R>>, LC extends LogContext<?, LC>> extends Table implements IRoleTable<R>, Loggable<LC> {

    protected final long userId;
    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    protected R role;
    protected boolean accepted;

    protected ExtendedRoleTable(final OffsetDateTime createdAt, final long id, final long userId, final R role, final boolean accepted) {
        super(createdAt, id);
        this.userId = userId;
        this.role = role;
        this.accepted = accepted;
    }

    protected ExtendedRoleTable(final long userId, final R role, final boolean accepted) {
        this.userId = userId;
        this.role = role;
        this.accepted = accepted;
    }

    @Override
    public long getUserId() {
        return this.userId;
    }

    @Override
    @JsonIgnore
    public R getRole() {
        return this.role;
    }

    @Override
    public void setRole(final R role) {
        this.role = role;
    }

    @Override
    public long getRoleId() {
        return this.role.getRoleId();
    }

    @Override
    @JsonIgnore
    public String getRoleType() {
        return this.role.getValue();
    }

    @Override
    public boolean isAccepted() {
        return this.accepted;
    }

    @Override
    public void setAccepted(final boolean accepted) {
        this.accepted = accepted;
    }

    @JdbiProperty(map = false)
    public abstract long getPrincipalId();

    @Override
    public String toString() {
        return "ExtendedRoleTable{" +
            "userId=" + this.userId +
            ", role=" + this.role +
            ", accepted=" + this.accepted +
            "} " + super.toString();
    }
}
