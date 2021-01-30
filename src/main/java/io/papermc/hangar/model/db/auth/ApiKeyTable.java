package io.papermc.hangar.model.db.auth;

import io.papermc.hangar.model.Named;
import io.papermc.hangar.model.Permission;
import io.papermc.hangar.model.db.Table;
import org.jdbi.v3.core.mapper.PropagateNull;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

import java.time.OffsetDateTime;

public class ApiKeyTable extends Table implements Named {

    private final String name;
    private final long ownerId;
    private final String tokenIdentifier;
    private final String token;
    private final Permission permissions;

    public ApiKeyTable(String name, long ownerId, String tokenIdentifier, String token, Permission permissions) {
        this.name = name;
        this.ownerId = ownerId;
        this.tokenIdentifier = tokenIdentifier;
        this.token = token;
        this.permissions = permissions;
    }

    @JdbiConstructor
    public ApiKeyTable(OffsetDateTime createdAt, @PropagateNull long id, String name, long ownerId, String tokenIdentifier, String token, Permission permissions) {
        super(createdAt, id);
        this.name = name;
        this.ownerId = ownerId;
        this.tokenIdentifier = tokenIdentifier;
        this.token = token;
        this.permissions = permissions;
    }

    @Override
    public String getName() {
        return name;
    }

    public long getOwnerId() {
        return ownerId;
    }

    public String getTokenIdentifier() {
        return tokenIdentifier;
    }

    public String getToken() {
        return token;
    }

    public Permission getPermissions() {
        return permissions;
    }

    @Override
    public String toString() {
        return "ApiKeyTable{" +
                "name='" + name + '\'' +
                ", ownerId=" + ownerId +
                ", tokenIdentifier='" + tokenIdentifier + '\'' +
                ", token='" + token + '\'' +
                ", permissions=" + permissions +
                "} " + super.toString();
    }
}
