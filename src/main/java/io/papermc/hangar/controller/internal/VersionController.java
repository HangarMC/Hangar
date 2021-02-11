package io.papermc.hangar.controller.internal;

import io.papermc.hangar.controller.HangarController;
import io.papermc.hangar.model.common.PermissionType;
import io.papermc.hangar.model.internal.versions.PendingVersion;
import io.papermc.hangar.modelold.NamedPermission;
import io.papermc.hangar.security.annotations.permission.PermissionRequired;
import io.papermc.hangar.security.annotations.unlocked.Unlocked;
import io.papermc.hangar.service.internal.versions.VersionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;

@Controller
@Secured("ROLE_USER")
@RequestMapping(path = "/api/internal/versions")
public class VersionController extends HangarController {

    private final VersionFactory versionFactory;

    @Autowired
    public VersionController(VersionFactory versionFactory) {
        this.versionFactory = versionFactory;
    }

    @Unlocked
    @PermissionRequired(type = PermissionType.PROJECT, perms = NamedPermission.CREATE_VERSION, args = "{#projectId}")
    @PostMapping(path = "/version/{id}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PendingVersion> createFromFile(@PathVariable("id") long projectId, @RequestParam("pluginFile") MultipartFile pluginFile) {
        return ResponseEntity.ok(versionFactory.createPendingVersion(projectId, pluginFile));
    }

    @Unlocked
    @PermissionRequired(type = PermissionType.PROJECT, perms = NamedPermission.CREATE_VERSION, args = "{#projectId}")
    @PostMapping(path = "/version/{id}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, params = "url")
    public ResponseEntity<PendingVersion> createFromUrl(@PathVariable("id") long projectId, @RequestParam String url) {
        return ResponseEntity.ok(versionFactory.createPendingVersion(projectId, url));
    }

    @Unlocked
    @ResponseStatus(HttpStatus.OK)
    @PermissionRequired(type = PermissionType.PROJECT, perms = NamedPermission.CREATE_VERSION, args = "{#projectId}")
    @PostMapping(path = "/version/{id}/create", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void createVersion(@PathVariable("id") long projectId, @RequestBody @Valid PendingVersion pendingVersion) {
        System.out.println(pendingVersion);
        versionFactory.publishPendingVersion(projectId, pendingVersion);
    }
}
