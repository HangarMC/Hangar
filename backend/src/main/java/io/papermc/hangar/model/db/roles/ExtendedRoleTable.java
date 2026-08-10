package io.papermc.hangar.model.db.roles;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.papermc.hangar.model.common.NamedPermission;
import io.papermc.hangar.model.common.Permission;
import io.papermc.hangar.model.db.Table;
import io.papermc.hangar.model.internal.logs.contexts.LogContext;
import io.papermc.hangar.model.loggable.Loggable;
import java.time.OffsetDateTime;
import java.util.List;
import org.jdbi.v3.core.annotation.JdbiProperty;

public abstract class ExtendedRoleTable<LC extends LogContext<?, LC>> extends Table implements IRoleTable, Loggable<LC> {

    protected final long userId;
    protected Permission permissions;
    protected String title;
    protected boolean accepted;
    protected boolean owner;

    protected ExtendedRoleTable(final OffsetDateTime createdAt, final long id, final long userId, final Permission permissions, final String title, final boolean accepted, final boolean owner) {
        super(createdAt, id);
        this.userId = userId;
        this.permissions = permissions;
        this.title = title;
        this.accepted = accepted;
        this.owner = owner;
    }

    protected ExtendedRoleTable(final long userId, final Permission permissions, final String title, final boolean accepted, final boolean owner) {
        this.userId = userId;
        this.permissions = permissions;
        this.title = title;
        this.accepted = accepted;
        this.owner = owner;
    }

    @Override
    public long getUserId() {
        return this.userId;
    }

    @JsonIgnore
    public Permission getPermissions() {
        return this.permissions;
    }

    public void setPermissions(final Permission permissions) {
        this.permissions = permissions;
    }

    @JsonProperty("permissions")
    @JdbiProperty(map = false)
    public List<NamedPermission> getNamedPermissions() {
        return this.permissions.toNamed();
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    @Override
    public boolean isAccepted() {
        return this.accepted;
    }

    @Override
    public void setAccepted(final boolean accepted) {
        this.accepted = accepted;
    }

    public boolean isOwner() {
        return this.owner;
    }

    public void setOwner(final boolean owner) {
        this.owner = owner;
    }

    @JdbiProperty(map = false)
    public abstract long getPrincipalId();

    @Override
    public String toString() {
        return "ExtendedRoleTable{" +
            "userId=" + this.userId +
            ", permissions=" + this.permissions +
            ", title='" + this.title + '\'' +
            ", accepted=" + this.accepted +
            ", owner=" + this.owner +
            "} " + super.toString();
    }
}
