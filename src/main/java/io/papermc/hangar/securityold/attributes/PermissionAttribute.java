package io.papermc.hangar.securityold.attributes;

import io.papermc.hangar.modelold.NamedPermission;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.util.Assert;

public abstract class PermissionAttribute implements ConfigAttribute {

    private final NamedPermission permission;
    private final String type;

    public PermissionAttribute(NamedPermission permission, String type) {
        this.type = type;
        Assert.notNull(permission, "You must provide a NamedPermission");
        this.permission = permission;
    }

    public NamedPermission getPermission() {
        return permission;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PermissionAttribute)) return false;

        PermissionAttribute that = (PermissionAttribute) obj;

        return that.permission == this.permission;
    }

    @Override
    public String getAttribute() {
        return null;
    }

    public String getType() {
        return type;
    }

    @Override
    public int hashCode() {
        return this.permission.hashCode();
    }

    @Override
    public String toString() {
        return this.permission.toString();
    }

    public static final String GLOBAL_TYPE = "GLOBAL";
    public static final String PROJECT_TYPE = "PROJECT";
    public static final String ORG_TYPE = "ORGANIZATION";
}
