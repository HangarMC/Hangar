package io.papermc.hangar.model.api;

import io.papermc.hangar.model.Model;
import io.papermc.hangar.model.common.NamedPermission;
import io.papermc.hangar.model.common.Permission;
import java.time.OffsetDateTime;
import java.util.List;

public class ApiKey extends Model {

    private final String name;
    private final String tokenIdentifier;
    private final List<NamedPermission> permissions;

    public ApiKey(final OffsetDateTime createdAt, final String name, final String tokenIdentifier, final Permission permissions) {
        super(createdAt);
        this.name = name;
        this.tokenIdentifier = tokenIdentifier;
        this.permissions = permissions.toNamed();
    }

    public String getName() {
        return this.name;
    }

    public String getTokenIdentifier() {
        return this.tokenIdentifier;
    }

    public List<NamedPermission> getPermissions() {
        return this.permissions;
    }
}
