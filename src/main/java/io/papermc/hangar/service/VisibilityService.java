package io.papermc.hangar.service;

import io.papermc.hangar.db.model.UsersTable;
import io.papermc.hangar.db.model.VisibilityModel;
import io.papermc.hangar.model.Permission;
import io.papermc.hangar.model.Visibility;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class VisibilityService extends HangarService {

    private final PermissionService permissionService;

    @Autowired
    public VisibilityService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @Nullable
    public <T extends VisibilityModel> T checkVisibility(@Nullable T model, Function<T, Long> projectIdSupplier) {
        if (model == null) {
            return null;
        }
        return checkVisibility(model, permissionService.getProjectPermissions(currentUser.get().map(UsersTable::getId).orElse(-10L), projectIdSupplier.apply(model)));
    }

    @Nullable
    public <T extends VisibilityModel> T checkVisibility(@Nullable T model, Permission permission) {
        if (model == null) {
            return null;
        }
        if (!permission.has(Permission.SeeHidden) && !permission.has(Permission.IsProjectMember) && model.getVisibility() != Visibility.PUBLIC) {
            return null;
        }
        return model;
    }
}
