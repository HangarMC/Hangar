package io.papermc.hangar.controller.internal;

import io.papermc.hangar.controller.HangarController;
import io.papermc.hangar.db.customtypes.LoggedActionType;
import io.papermc.hangar.db.customtypes.LoggedActionType.VersionContext;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.common.NamedPermission;
import io.papermc.hangar.model.common.PermissionType;
import io.papermc.hangar.model.db.versions.ProjectVersionTable;
import io.papermc.hangar.model.internal.api.requests.StringContent;
import io.papermc.hangar.model.internal.versions.HangarVersion;
import io.papermc.hangar.model.internal.versions.PendingVersion;
import io.papermc.hangar.security.annotations.permission.PermissionRequired;
import io.papermc.hangar.security.annotations.unlocked.Unlocked;
import io.papermc.hangar.security.annotations.visibility.VisibilityRequired;
import io.papermc.hangar.security.annotations.visibility.VisibilityRequired.Type;
import io.papermc.hangar.service.internal.versions.VersionFactory;
import io.papermc.hangar.service.internal.versions.VersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;

@Controller
@Secured("ROLE_USER")
@RequestMapping(path = "/api/internal/versions")
public class VersionController extends HangarController {

    private final VersionFactory versionFactory;
    private final VersionService versionService;

    @Autowired
    public VersionController(VersionFactory versionFactory, VersionService versionService) {
        this.versionFactory = versionFactory;
        this.versionService = versionService;
    }

    @VisibilityRequired(type = Type.PROJECT, args = "{#author, #slug}")
    @GetMapping(path = "/version/{author}/{slug}/versions/{versionString}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<HangarVersion>> getVersions(@PathVariable String author, @PathVariable String slug, @PathVariable String versionString) {
        return ResponseEntity.ok(versionService.getHangarVersions(author, slug, versionString));
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
        PendingVersion pendingVersion = versionFactory.createPendingVersion(projectId, url);
        System.out.println(pendingVersion);
        return ResponseEntity.ok(pendingVersion);
    }

    @Unlocked
    @ResponseStatus(HttpStatus.OK)
    @PermissionRequired(type = PermissionType.PROJECT, perms = NamedPermission.CREATE_VERSION, args = "{#projectId}")
    @PostMapping(path = "/version/{id}/create", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void createVersion(@PathVariable("id") long projectId, @RequestBody @Valid PendingVersion pendingVersion) {
        System.out.println(pendingVersion);
        versionFactory.publishPendingVersion(projectId, pendingVersion);
    }

    @Unlocked
    @ResponseStatus(HttpStatus.OK)
    @PermissionRequired(type = PermissionType.PROJECT, perms = NamedPermission.EDIT_VERSION, args = "{#projectId}")
    @PostMapping(path = "/version/{projectId}/{versionId}/saveDescription", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void saveDescription(@PathVariable long projectId, @PathVariable long versionId, @Valid @RequestBody StringContent stringContent) {
        ProjectVersionTable projectVersionTable = versionService.getProjectVersionTable(versionId);
        if (projectVersionTable == null) {
            throw new HangarApiException(HttpStatus.NOT_FOUND);
        }
        String oldDesc = projectVersionTable.getDescription();
        String newDesc = stringContent.getContent().trim();
        projectVersionTable.setDescription(newDesc);
        versionService.updateProjectVersionTable(projectVersionTable);
        userActionLogService.version(LoggedActionType.VERSION_DESCRIPTION_CHANGED.with(VersionContext.of(projectId, versionId)), newDesc, oldDesc);
    }
}
