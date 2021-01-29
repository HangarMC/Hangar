package io.papermc.hangar.service;

import io.papermc.hangar.model.Permission;
import io.papermc.hangar.model.Visibility;
import io.papermc.hangar.model.Visible;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.function.Function;
import java.util.function.Supplier;

@Service
public class VisibilityService extends HangarService {

    private final PermissionService permissionService;


    @Autowired
    public VisibilityService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @Nullable
    public <T extends Visible> T checkVisibility(@Nullable T model, @NotNull Function<T, Long> projectIdSupplier) {
        return _checkVisibility(model, hangarRequest::getUserId, projectIdSupplier);
    }

    @Nullable
    public <T extends Visible> T checkVisibility(@Nullable T model, @NotNull Permission permission) {
        if (model == null) {
            return null;
        }
        if (!permission.has(Permission.SeeHidden) && !permission.has(Permission.IsProjectMember) && model.getVisibility() != Visibility.PUBLIC) {
            return null;
        }
        return model;
    }

    public <T extends Visible> T checkApiVisibility(@Nullable T model, @NotNull Function<T, Long> projectIdSupplier) {
        return _checkVisibility(model, hangarApiRequest::getUserId, projectIdSupplier);
    }

    private <T extends Visible> T _checkVisibility(@Nullable T model, @NotNull Supplier<Long> userIdSupplier, @NotNull Function<T, Long> projectIdSupplier) {
        if (model == null) {
            return null;
        }
        return checkVisibility(model, permissionService.getProjectPermissions(userIdSupplier.get(), projectIdSupplier.apply(model)));
    }
}
