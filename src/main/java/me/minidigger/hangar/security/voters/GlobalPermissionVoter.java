package me.minidigger.hangar.security.voters;

import me.minidigger.hangar.model.NamedPermission;
import me.minidigger.hangar.service.PermissionService;

import java.util.Collection;
import java.util.Set;

public class GlobalPermissionVoter extends HangarPermissionVoter {

    public GlobalPermissionVoter(PermissionService permissionService) {
        super(hangarAuth -> {
            Collection<NamedPermission> globalPerms = permissionService.getGlobalPermissions(hangarAuth.getUserId()).toNamed();
            if (globalPerms.isEmpty()) globalPerms = Set.of(NamedPermission.HARD_DELETE_PROJECT, NamedPermission.SEE_HIDDEN); // TODO for development
            return globalPerms;
        }, hangarAuth -> true);
    }
}
