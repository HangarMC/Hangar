package io.papermc.hangar.model.db.auth;

import io.papermc.hangar.model.Named;
import io.papermc.hangar.model.common.Permission;
import io.papermc.hangar.model.db.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

public class ApiKeyTable extends Table implements Named {

    private final String name;
    private final long ownerId;
    private final UUID tokenIdentifier;
    private final String token;
    private final Permission permissions;
    private OffsetDateTime lastUsed = null;

    public ApiKeyTable(final String name, final long ownerId, final UUID tokenIdentifier, final String token, final Permission permissions) {
        this.name = name;
        this.ownerId = ownerId;
        this.tokenIdentifier = tokenIdentifier;
        this.token = token;
        this.permissions = permissions;
    }

    @JdbiConstructor
    public ApiKeyTable(final OffsetDateTime createdAt, final long id, final String name, final long ownerId, final UUID tokenIdentifier, final String token, final Permission permissions, final OffsetDateTime lastUsed) {
        super(createdAt, id);
        this.name = name;
        this.ownerId = ownerId;
        this.tokenIdentifier = tokenIdentifier;
        this.token = token;
        this.permissions = permissions;
        this.lastUsed = lastUsed;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public long getOwnerId() {
        return this.ownerId;
    }

    public UUID getTokenIdentifier() {
        return this.tokenIdentifier;
    }

    public String getToken() {
        return this.token;
    }

    public Permission getPermissions() {
        return this.permissions;
    }

    public OffsetDateTime getLastUsed() {
        return this.lastUsed;
    }

    public void setLastUsed(final OffsetDateTime lastUsed) {
        this.lastUsed = lastUsed;
    }

    @Override
    public String toString() {
        return "ApiKeyTable{" +
            "name='" + this.name + '\'' +
            ", ownerId=" + this.ownerId +
            ", tokenIdentifier='" + this.tokenIdentifier + '\'' +
            ", token='" + this.token + '\'' +
            ", permissions=" + this.permissions +
            ", lastUsed=" + this.lastUsed +
            "} " + super.toString();
    }
}
