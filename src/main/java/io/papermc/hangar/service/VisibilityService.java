package io.papermc.hangar.service;

import io.papermc.hangar.db.modelold.UsersTable;
import io.papermc.hangar.model.Visible;
import io.papermc.hangar.model.Permission;
import io.papermc.hangar.model.Visibility;
import io.papermc.hangar.modelold.ApiAuthInfo;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

@Service
public class VisibilityService extends HangarService {

    private final PermissionService permissionService;

    private final ApiAuthInfo apiAuthInfo;

    @Autowired
    public VisibilityService(PermissionService permissionService, ApiAuthInfo apiAuthInfo) {
        this.permissionService = permissionService;
        this.apiAuthInfo = apiAuthInfo;
    }

    @Nullable
    public <T extends Visible> T checkVisibility(@Nullable T model, Function<T, Long> projectIdSupplier) {
        return _checkVisibility(model, () -> currentUser.get().map(UsersTable::getId).orElse(-10L), projectIdSupplier);
    }

    @Nullable
    public <T extends Visible> T checkVisibility(@Nullable T model, Permission permission) {
        if (model == null) {
            return null;
        }
        if (!permission.has(Permission.SeeHidden) && !permission.has(Permission.IsProjectMember) && model.getVisibility() != Visibility.PUBLIC) {
            return null;
        }
        return model;
    }

    public <T extends Visible> T checkApiVisibility(@Nullable T model, Function<T, Long> projectIdSupplier) {
        return _checkVisibility(model, () -> Optional.ofNullable(apiAuthInfo.getUser()).map(UsersTable::getId).orElse(-10L), projectIdSupplier);
    }

    private <T extends Visible> T _checkVisibility(@Nullable T model, Supplier<Long> userIdSupplier, Function<T, Long> projectIdSupplier) {
        if (model == null) {
            return null;
        }
        return checkVisibility(model, permissionService.getProjectPermissions(userIdSupplier.get(), projectIdSupplier.apply(model)));
    }
}
