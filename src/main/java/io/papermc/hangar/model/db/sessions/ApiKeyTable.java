package io.papermc.hangar.model.db.sessions;

import io.papermc.hangar.model.Named;
import io.papermc.hangar.model.Permission;
import io.papermc.hangar.model.db.Table;
import org.jdbi.v3.core.mapper.Nested;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

import java.time.OffsetDateTime;

public class ApiKeyTable extends Table implements Named {

    private final String name;
    private final long ownerId;
    private final String tokenIdentifier;
    private final String token;
    private final Permission permission;

    public ApiKeyTable(String name, long ownerId, String tokenIdentifier, String token, Permission permission) {
        this.name = name;
        this.ownerId = ownerId;
        this.tokenIdentifier = tokenIdentifier;
        this.token = token;
        this.permission = permission;
    }

    @JdbiConstructor
    public ApiKeyTable(OffsetDateTime createdAt, long id, String name, long ownerId, String tokenIdentifier, String token, @Nested("perm") Permission permission) {
        super(createdAt, id);
        this.name = name;
        this.ownerId = ownerId;
        this.tokenIdentifier = tokenIdentifier;
        this.token = token;
        this.permission = permission;
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

    public Permission getPermission() {
        return permission;
    }
}
