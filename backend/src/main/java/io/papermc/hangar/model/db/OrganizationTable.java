package io.papermc.hangar.model.db;

import io.papermc.hangar.model.Owned;
import io.papermc.hangar.model.Visitable;
import io.papermc.hangar.model.db.projects.ProjectOwner;
import io.papermc.hangar.model.loggable.OrganizationLoggable;
import org.jdbi.v3.core.mapper.PropagateNull;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;
import java.time.OffsetDateTime;
import java.util.UUID;

public class OrganizationTable extends Table implements Visitable, ProjectOwner, OrganizationLoggable, Owned {

    private final String name;
    private long ownerId;
    private final long userId;
    private final UUID userUuid;

    @JdbiConstructor
    public OrganizationTable(final OffsetDateTime createdAt, @PropagateNull final long id, final String name, final long ownerId, final long userId, @org.springframework.lang.Nullable final UUID userUuid) {
        super(createdAt, id);
        this.name = name;
        this.ownerId = ownerId;
        this.userId = userId;
        this.userUuid = userUuid;
    }

    public OrganizationTable(final long id, final String name, final long ownerId, final long userId, final UUID userUuid) {
        super(id);
        this.name = name;
        this.ownerId = ownerId;
        this.userId = userId;
        this.userUuid = userUuid;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public long getOwnerId() {
        return this.ownerId;
    }

    public void setOwnerId(final long ownerId) {
        this.ownerId = ownerId;
    }

    @Override
    public long getUserId() {
        return this.userId;
    }

    @Override
    public boolean isOrganization() {
        return true;
    }

    @Override
    public String getUrl() {
        return "/" + this.getName();
    }

    @Override
    public long getOrganizationId() {
        return this.id;
    }

    public UUID getUserUuid() {
        return this.userUuid;
    }

    @Override
    public String toString() {
        return "OrganizationTable{" +
            "name='" + this.name + '\'' +
            ", ownerId=" + this.ownerId +
            ", userId=" + this.userId +
            "} " + super.toString();
    }
}
