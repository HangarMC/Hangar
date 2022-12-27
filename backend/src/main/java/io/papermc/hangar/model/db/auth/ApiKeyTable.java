package io.papermc.hangar.model.db.auth;

import io.papermc.hangar.model.Named;
import io.papermc.hangar.model.common.Permission;
import io.papermc.hangar.model.db.Table;
import java.time.OffsetDateTime;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

public class ApiKeyTable extends Table implements Named {

    private final String name;
    private final long ownerId;
    private final String tokenIdentifier;
    private final String token;
    private final Permission permissions;

    public ApiKeyTable(final String name, final long ownerId, final String tokenIdentifier, final String token, final Permission permissions) {
        this.name = name;
        this.ownerId = ownerId;
        this.tokenIdentifier = tokenIdentifier;
        this.token = token;
        this.permissions = permissions;
    }

    @JdbiConstructor
    public ApiKeyTable(final OffsetDateTime createdAt, final long id, final String name, final long ownerId, final String tokenIdentifier, final String token, final Permission permissions) {
        super(createdAt, id);
        this.name = name;
        this.ownerId = ownerId;
        this.tokenIdentifier = tokenIdentifier;
        this.token = token;
        this.permissions = permissions;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public long getOwnerId() {
        return this.ownerId;
    }

    public String getTokenIdentifier() {
        return this.tokenIdentifier;
    }

    public String getToken() {
        return this.token;
    }

    public Permission getPermissions() {
        return this.permissions;
    }

    @Override
    public String toString() {
        return "ApiKeyTable{" +
            "name='" + this.name + '\'' +
            ", ownerId=" + this.ownerId +
            ", tokenIdentifier='" + this.tokenIdentifier + '\'' +
            ", token='" + this.token + '\'' +
            ", permissions=" + this.permissions +
            "} " + super.toString();
    }
}
