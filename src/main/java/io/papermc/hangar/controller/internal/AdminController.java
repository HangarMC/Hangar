package io.papermc.hangar.controller.internal;

import io.papermc.hangar.controller.HangarController;
import io.papermc.hangar.model.common.NamedPermission;
import io.papermc.hangar.model.internal.api.requests.admin.ChangePlatformVersionsForm;
import io.papermc.hangar.security.annotations.permission.PermissionRequired;
import io.papermc.hangar.service.internal.PlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.validation.Valid;

@Controller
@RequestMapping("/api/internal/admin")
public class AdminController extends HangarController {

    private final PlatformService platformService;

    @Autowired
    public AdminController(PlatformService platformService) {
        this.platformService = platformService;
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping(path = "/platformVersions", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PermissionRequired(perms = NamedPermission.MANUAL_VALUE_CHANGES)
    public void changePlatformVersions(@RequestBody @Valid ChangePlatformVersionsForm form) {
        platformService.updatePlatformVersions(form);
    }
}
