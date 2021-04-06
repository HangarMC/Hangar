package io.papermc.hangar.model.api;

import io.papermc.hangar.model.Model;
import io.papermc.hangar.model.common.NamedPermission;
import io.papermc.hangar.model.common.Permission;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class ApiKey extends Model {

    private final String name;
    private final String tokenIdentifier;
    private final List<String> permissions;

    public ApiKey(OffsetDateTime createdAt, String name, String tokenIdentifier, Permission permissions) {
        super(createdAt);
        this.name = name;
        this.tokenIdentifier = tokenIdentifier;
        this.permissions = permissions.toNamed().stream().map(NamedPermission::getValue).collect(Collectors.toList());
    }

    public String getName() {
        return name;
    }

    public String getTokenIdentifier() {
        return tokenIdentifier;
    }

    public List<String> getPermissions() {
        return permissions;
    }
}
