package io.papermc.hangar.securityold.attributes;

import io.papermc.hangar.modelold.NamedPermission;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

public class ProjectPermissionAttribute extends PermissionAttribute {

    public ProjectPermissionAttribute(NamedPermission permission) {
        super(permission, PermissionAttribute.PROJECT_TYPE);
    }

    public static List<ConfigAttribute> createList(NamedPermission...permissions) {
        Assert.notNull(permissions, "You must supply an array of permissions");
        List<ConfigAttribute> attributes = new ArrayList<>(permissions.length);
        for (NamedPermission permission : permissions) {
            attributes.add(new ProjectPermissionAttribute(permission));
        }
        return attributes;
    }
}
