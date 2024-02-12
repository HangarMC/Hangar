package io.papermc.hangar.model.api;

import io.papermc.hangar.model.Model;
import io.papermc.hangar.model.common.NamedPermission;
import io.papermc.hangar.model.common.Permission;
import java.time.OffsetDateTime;
import java.util.List;
import org.checkerframework.checker.nullness.qual.Nullable;

public class ApiKey extends Model {

    private final String name;
    private final String tokenIdentifier;
    private final List<NamedPermission> permissions;
    private final @Nullable OffsetDateTime lastUsed;

    public ApiKey(final OffsetDateTime createdAt, final String name, final String tokenIdentifier, final Permission permissions, final @Nullable OffsetDateTime lastUsed) {
        super(createdAt);
        this.name = name;
        this.tokenIdentifier = tokenIdentifier;
        this.permissions = permissions.toNamed();
        this.lastUsed = lastUsed;
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

    public @Nullable OffsetDateTime getLastUsed() {
        return this.lastUsed;
    }
}
