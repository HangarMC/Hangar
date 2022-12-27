package io.papermc.hangar.model.db;

import io.papermc.hangar.model.Owned;
import io.papermc.hangar.model.Visitable;
import io.papermc.hangar.model.db.projects.ProjectOwner;
import io.papermc.hangar.model.loggable.OrganizationLoggable;
import org.jdbi.v3.core.mapper.PropagateNull;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

import java.time.OffsetDateTime;

public class OrganizationTable extends Table implements Visitable, ProjectOwner, OrganizationLoggable, Owned {

    private final String name;
    private long ownerId;
    private final long userId;

    @JdbiConstructor
    public OrganizationTable(final OffsetDateTime createdAt, @PropagateNull final long id, final String name, final long ownerId, final long userId) {
        super(createdAt, id);
        this.name = name;
        this.ownerId = ownerId;
        this.userId = userId;
    }

    public OrganizationTable(final long id, final String name, final long ownerId, final long userId) {
        super(id);
        this.name = name;
        this.ownerId = ownerId;
        this.userId = userId;
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

    @Override
    public String toString() {
        return "OrganizationTable{" +
                "name='" + this.name + '\'' +
                ", ownerId=" + this.ownerId +
                ", userId=" + this.userId +
                "} " + super.toString();
    }
}
