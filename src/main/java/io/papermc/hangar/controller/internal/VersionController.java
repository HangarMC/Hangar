package io.papermc.hangar.controller.internal;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.common.NamedPermission;
import io.papermc.hangar.model.common.PermissionType;
import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.model.db.projects.ProjectTable;
import io.papermc.hangar.model.db.versions.ProjectVersionTable;
import io.papermc.hangar.model.internal.api.requests.StringContent;
import io.papermc.hangar.model.internal.api.requests.versions.UpdatePlatformVersions;
import io.papermc.hangar.model.internal.api.requests.versions.UpdatePluginDependencies;
import io.papermc.hangar.model.internal.logs.LogAction;
import io.papermc.hangar.model.internal.logs.contexts.VersionContext;
import io.papermc.hangar.model.internal.versions.HangarVersion;
import io.papermc.hangar.model.internal.versions.LastDependencies;
import io.papermc.hangar.model.internal.versions.PendingVersion;
import io.papermc.hangar.model.internal.versions.MultipartFileOrUrl;
import io.papermc.hangar.security.annotations.permission.PermissionRequired;
import io.papermc.hangar.security.annotations.ratelimit.RateLimit;
import io.papermc.hangar.security.annotations.unlocked.Unlocked;
import io.papermc.hangar.security.annotations.visibility.VisibilityRequired;
import io.papermc.hangar.security.annotations.visibility.VisibilityRequired.Type;
import io.papermc.hangar.service.internal.versions.DownloadService;
import io.papermc.hangar.service.internal.versions.PinnedVersionService;
import io.papermc.hangar.service.internal.versions.VersionDependencyService;
import io.papermc.hangar.service.internal.versions.VersionFactory;
import io.papermc.hangar.service.internal.versions.VersionService;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import javax.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@Secured("ROLE_USER")
@RateLimit(path = "version")
@RequestMapping(path = "/api/internal/versions")
public class VersionController extends HangarComponent {

    private final VersionFactory versionFactory;
    private final VersionService versionService;
    private final VersionDependencyService versionDependencyService;
    private final DownloadService downloadService;
    private final PinnedVersionService pinnedVersionService;

    @Autowired
    public VersionController(VersionFactory versionFactory, VersionService versionService, VersionDependencyService versionDependencyService, DownloadService downloadService, final PinnedVersionService pinnedVersionService) {
        this.versionFactory = versionFactory;
        this.versionService = versionService;
        this.versionDependencyService = versionDependencyService;
        this.downloadService = downloadService;
        this.pinnedVersionService = pinnedVersionService;
    }

    @VisibilityRequired(type = Type.PROJECT, args = "{#author, #slug}")
    @GetMapping(path = "/version/{author}/{slug}/versions/{versionString}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<HangarVersion> getVersion(@PathVariable String author, @PathVariable String slug, @PathVariable String versionString) {
        return ResponseEntity.ok(versionService.getHangarVersion(author, slug, versionString));
    }

    @Unlocked
    @RateLimit(overdraft = 5, refillTokens = 1, refillSeconds = 5)
    @PermissionRequired(type = PermissionType.PROJECT, perms = NamedPermission.CREATE_VERSION, args = "{#projectId}")
    @PostMapping(path = "/version/{id}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PendingVersion> create(@PathVariable("id") long projectId, @RequestBody @Size(min = 1, max = 3, message = "version.new.error.invalidNumOfPlatforms") List<@Valid MultipartFileOrUrl> files) {
        return ResponseEntity.ok(versionFactory.createPendingVersion(projectId, files));
    }

    @Unlocked
    @RateLimit(overdraft = 3, refillTokens = 1, refillSeconds = 30)
    @ResponseStatus(HttpStatus.OK)
    @PermissionRequired(type = PermissionType.PROJECT, perms = NamedPermission.CREATE_VERSION, args = "{#projectId}")
    @PostMapping(path = "/version/{id}/create", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void createVersion(@PathVariable("id") long projectId, @RequestBody @Valid PendingVersion pendingVersion) {
        versionFactory.publishPendingVersion(projectId, pendingVersion);
    }

    @Unlocked
    @RateLimit(overdraft = 5, refillTokens = 1, refillSeconds = 10)
    @ResponseStatus(HttpStatus.OK)
    @PermissionRequired(type = PermissionType.PROJECT, perms = NamedPermission.EDIT_VERSION, args = "{#projectId}")
    @PostMapping(path = "/version/{projectId}/{versionId}/saveDescription", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void saveDescription(@PathVariable long projectId, @PathVariable long versionId, @Valid @RequestBody StringContent stringContent) {
        if (stringContent.getContent().length() > config.pages.getMaxLen()) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "page.new.error.maxLength");
        }

        ProjectVersionTable projectVersionTable = versionService.getProjectVersionTable(versionId);
        if (projectVersionTable == null) {
            throw new HangarApiException(HttpStatus.NOT_FOUND);
        }
        String oldDesc = projectVersionTable.getDescription();
        String newDesc = stringContent.getContent().trim();
        projectVersionTable.setDescription(newDesc);
        versionService.updateProjectVersionTable(projectVersionTable);
        actionLogger.version(LogAction.VERSION_DESCRIPTION_CHANGED.create(VersionContext.of(projectId, versionId), newDesc, oldDesc));
    }

    @Unlocked
    @ResponseStatus(HttpStatus.OK)
    @PermissionRequired(type = PermissionType.PROJECT, perms = NamedPermission.EDIT_VERSION, args = "{#projectId}")
    @RateLimit(overdraft = 5, refillTokens = 1, refillSeconds = 10)
    @PostMapping(path = "/version/{projectId}/{versionId}/savePlatformVersions", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void savePlatformVersions(@PathVariable long projectId, @PathVariable long versionId, @Valid @RequestBody UpdatePlatformVersions updatePlatformVersions) {
        versionDependencyService.updateVersionPlatformVersions(projectId, versionId, updatePlatformVersions);
    }

    @Unlocked
    @ResponseStatus(HttpStatus.OK)
    @PermissionRequired(type = PermissionType.PROJECT, perms = NamedPermission.EDIT_VERSION, args = "{#projectId}")
    @RateLimit(overdraft = 5, refillTokens = 1, refillSeconds = 10)
    @PostMapping(path = "/version/{projectId}/{versionId}/savePluginDependencies", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void savePluginDependencies(@PathVariable long projectId, @PathVariable long versionId, @Valid @RequestBody UpdatePluginDependencies updatePluginDependencies) {
        versionDependencyService.updateVersionPluginDependencies(projectId, versionId, updatePluginDependencies);
    }

    @Unlocked
    @ResponseStatus(HttpStatus.OK)
    @RateLimit(overdraft = 5, refillTokens = 1, refillSeconds = 10)
    @PermissionRequired(type = PermissionType.PROJECT, perms = NamedPermission.EDIT_SUBJECT_SETTINGS, args = "{#projectId}")
    @PostMapping(path = "/version/{projectId}/{versionId}/pinned")
    public void setPinnedStatus(@PathVariable final long projectId, @PathVariable final long versionId, @RequestParam final boolean value) {
        if (value) {
            this.pinnedVersionService.addPinnedVersion(projectId, versionId);
        } else {
            this.pinnedVersionService.deletePinnedVersion(projectId, versionId);
        }
    }

    @Unlocked
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RateLimit(overdraft = 3, refillTokens = 1, refillSeconds = 30)
    @PermissionRequired(type = PermissionType.PROJECT, perms = NamedPermission.DELETE_VERSION, args = "{#projectId}")
    @PostMapping(path = "/version/{projectId}/{versionId}/delete", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void softDeleteVersion(@PathVariable long projectId, @PathVariable("versionId") ProjectVersionTable version, @RequestBody @Valid StringContent commentContent) {
        versionService.softDeleteVersion(projectId, version, commentContent.getContent());
    }

    @Unlocked
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PermissionRequired(NamedPermission.HARD_DELETE_VERSION)
    @PostMapping(path = "/version/{projectId}/{versionId}/hardDelete", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void hardDeleteVersion(@PathVariable("projectId") ProjectTable projectTable, @PathVariable("versionId") ProjectVersionTable projectVersionTable, @RequestBody @Valid StringContent commentContent) {
        versionService.hardDeleteVersion(projectTable, projectVersionTable, commentContent.getContent());
    }

    @Unlocked
    @ResponseStatus(HttpStatus.CREATED)
    @PermissionRequired(type = PermissionType.PROJECT, perms = NamedPermission.DELETE_VERSION, args = "{#projectId}")
    @PostMapping("/version/{projectId}/{versionId}/restore")
    public void restoreVersion(@PathVariable long projectId, @PathVariable("versionId") ProjectVersionTable version) {
        versionService.restoreVersion(projectId, version);
    }

    @ResponseBody
    @RateLimit(overdraft = 5, refillTokens = 1, refillSeconds = 20)
    @VisibilityRequired(type = Type.VERSION, args = "{#author, #slug, #versionString, #platform}")
    @GetMapping(path = "/version/{author}/{slug}/versions/{versionString}/{platform}/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public FileSystemResource download(@PathVariable String author, @PathVariable String slug, @PathVariable String versionString, @PathVariable Platform platform, @RequestParam(required = false) String token) {
        return downloadService.getVersionFile(author, slug, versionString, platform, true, token);
    }

    @VisibilityRequired(type = Type.VERSION, args = "{#author, #slug, #versionString, #platform}")
    @GetMapping(path = "/version/{author}/{slug}/versions/{versionString}/{platform}/downloadCheck")
    public ResponseEntity<String> downloadCheck(@PathVariable String author, @PathVariable String slug, @PathVariable String versionString, @PathVariable Platform platform) {
        boolean requiresConfirmation = downloadService.requiresConfirmation(author, slug, versionString, platform);
        if (requiresConfirmation) {
            String token = downloadService.createConfirmationToken(author, slug, versionString);
            if (token == null) {
                // null means we already had confirmed, no reason to confirm again
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            }
            return ResponseEntity.status(HttpStatus.PRECONDITION_REQUIRED).body(token);
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @VisibilityRequired(type = Type.PROJECT, args = "{#author, #slug}")
    @RateLimit(overdraft = 7, refillTokens = 3, greedy = true)
    @GetMapping(path = "/version/{author}/{slug}/lastdependencies")
    public ResponseEntity<LastDependencies> getLastVersionDependencies(@PathVariable String author, @PathVariable String slug, @RequestParam(required = false) String channel, @RequestParam String platform) {
        return ResponseEntity.ok(versionService.getLastVersionDependencies(author, slug, channel, platform));
    }
}
