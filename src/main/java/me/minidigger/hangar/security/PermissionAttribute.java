package me.minidigger.hangar.security;

import me.minidigger.hangar.model.NamedPermission;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

public class PermissionAttribute implements ConfigAttribute {

    private final NamedPermission permission;

    public PermissionAttribute(NamedPermission permission) {
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

    @Override
    public int hashCode() {
        return this.permission.hashCode();
    }

    @Override
    public String toString() {
        return this.permission.toString();
    }

    public static List<ConfigAttribute> createList(NamedPermission...permissions) {
        Assert.notNull(permissions, "You must supply an array of permissions");
        List<ConfigAttribute> attributes = new ArrayList<>(permissions.length);
        for (NamedPermission permission : permissions) {
            attributes.add(new PermissionAttribute(permission));
        }
        return attributes;
    }
}
