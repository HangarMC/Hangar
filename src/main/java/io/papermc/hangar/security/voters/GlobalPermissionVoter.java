package io.papermc.hangar.security.voters;

import io.papermc.hangar.security.attributes.PermissionAttribute;
import io.papermc.hangar.service.PermissionService;

public class GlobalPermissionVoter extends HangarPermissionVoter {

    public GlobalPermissionVoter(PermissionService permissionService) {
        super((hangarAuth, ignored) -> permissionService.getGlobalPermissions(hangarAuth.getUserId()).toNamed(),
                hangarAuth -> true,
                permissionAttribute -> permissionAttribute.getType().equals(PermissionAttribute.GLOBAL_TYPE));
    }
}
