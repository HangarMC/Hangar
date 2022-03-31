package io.papermc.hangar.security.attributes;

import io.papermc.hangar.model.NamedPermission;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

public class GlobalPermissionAttribute extends PermissionAttribute {


    public GlobalPermissionAttribute(NamedPermission permission) {
        super(permission, PermissionAttribute.GLOBAL_TYPE);
    }

    public static List<ConfigAttribute> createList(NamedPermission...permissions) {
        Assert.notNull(permissions, "You must supply an array of permissions");
        List<ConfigAttribute> attributes = new ArrayList<>(permissions.length);
        for (NamedPermission permission : permissions) {
            attributes.add(new GlobalPermissionAttribute(permission));
        }
        return attributes;
    }
}
