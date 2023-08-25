package io.papermc.hangar.controller.internal;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.controller.internal.config.VersionControllerConfig;
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
import io.papermc.hangar.model.internal.versions.MultipartFileOrUrl;
import io.papermc.hangar.model.internal.versions.PendingVersion;
import io.papermc.hangar.security.annotations.Anyone;
import io.papermc.hangar.security.annotations.permission.PermissionRequired;
import io.papermc.hangar.security.annotations.ratelimit.RateLimit;
import io.papermc.hangar.security.annotations.unlocked.Unlocked;
import io.papermc.hangar.security.annotations.visibility.VisibilityRequired;
import io.papermc.hangar.service.internal.versions.DownloadService;
import io.papermc.hangar.service.internal.versions.PinnedVersionService;
import io.papermc.hangar.service.internal.versions.VersionDependencyService;
import io.papermc.hangar.service.internal.versions.VersionFactory;
import io.papermc.hangar.service.internal.versions.VersionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletComponentScan;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;

// @el(projectId: long, author: String, slug: String, versionString: String, platform: io.papermc.hangar.model.common.Platform)
@Controller
@Secured("ROLE_USER")
@RateLimit(path = "version")
@ServletComponentScan(basePackageClasses = VersionControllerConfig.class)
@RequestMapping(path = "/api/internal/versions")
public class VersionController extends HangarComponent {

    private final VersionFactory versionFactory;
    private final VersionService versionService;
    private final VersionDependencyService versionDependencyService;
    private final DownloadService downloadService;
    private final PinnedVersionService pinnedVersionService;

    @Autowired
    public VersionController(final VersionFactory versionFactory, final VersionService versionService, final VersionDependencyService versionDependencyService, final DownloadService downloadService, final PinnedVersionService pinnedVersionService) {
        this.versionFactory = versionFactory;
        this.versionService = versionService;
        this.versionDependencyService = versionDependencyService;
        this.downloadService = downloadService;
        this.pinnedVersionService = pinnedVersionService;
    }

    @VisibilityRequired(type = VisibilityRequired.Type.PROJECT, args = "{#slug}")
    @GetMapping(path = "/version/{slug}/versions/{versionString}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<HangarVersion> getVersion(@PathVariable final String slug, @PathVariable final String versionString) {
        return ResponseEntity.ok(this.versionService.getHangarVersion(slug, versionString));
    }

    @Unlocked
    @RateLimit(overdraft = 5, refillTokens = 1, refillSeconds = 5)
    @PermissionRequired(type = PermissionType.PROJECT, perms = NamedPermission.CREATE_VERSION, args = "{#projectId}")
    @PostMapping(path = "/version/{id}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PendingVersion> create(@PathVariable("id") final long projectId,
                                                 @RequestPart(required = false) @Size(max = 3, message = "version.new.error.invalidNumOfPlatforms") final List<@Valid MultipartFile> files,
                                                 @RequestPart @Size(min = 1, max = 3, message = "version.new.error.invalidNumOfPlatforms") final List<@Valid MultipartFileOrUrl> data,
                                                 @RequestPart @NotBlank final String channel) {
        // Use separate lists to hack around multipart form data limitations
        return ResponseEntity.ok(this.versionFactory.createPendingVersion(projectId, data, files, channel, true));
    }

    @Unlocked
    @RateLimit(overdraft = 3, refillTokens = 1, refillSeconds = 30)
    @ResponseStatus(HttpStatus.OK)
    @PermissionRequired(type = PermissionType.PROJECT, perms = NamedPermission.CREATE_VERSION, args = "{#projectId}")
    @PostMapping(path = "/version/{id}/create", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void createVersion(@PathVariable("id") final long projectId, @RequestBody @Valid final PendingVersion pendingVersion) {
        this.versionFactory.publishPendingVersion(projectId, pendingVersion);
    }

    @Unlocked
    @RateLimit(overdraft = 5, refillTokens = 1, refillSeconds = 10)
    @ResponseStatus(HttpStatus.OK)
    @PermissionRequired(type = PermissionType.PROJECT, perms = NamedPermission.EDIT_VERSION, args = "{#projectId}")
    @PostMapping(path = "/version/{projectId}/{versionId}/saveDescription", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void saveDescription(@PathVariable final long projectId, @PathVariable final long versionId, @RequestBody @Valid final StringContent stringContent) {
        if (stringContent.getContent().length() > this.config.pages.maxLen()) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "page.new.error.maxLength");
        }

        final ProjectVersionTable projectVersionTable = this.versionService.getProjectVersionTable(versionId);
        if (projectVersionTable == null) {
            throw new HangarApiException(HttpStatus.NOT_FOUND);
        }
        final String oldDesc = projectVersionTable.getDescription();
        final String newDesc = stringContent.getContent().trim();
        projectVersionTable.setDescription(newDesc);
        this.versionService.updateProjectVersionTable(projectVersionTable);
        this.actionLogger.version(LogAction.VERSION_DESCRIPTION_CHANGED.create(VersionContext.of(projectId, versionId), newDesc, oldDesc));
    }

    @Unlocked
    @ResponseStatus(HttpStatus.OK)
    @PermissionRequired(type = PermissionType.PROJECT, perms = NamedPermission.EDIT_VERSION, args = "{#projectId}")
    @RateLimit(overdraft = 5, refillTokens = 1, refillSeconds = 10)
    @PostMapping(path = "/version/{projectId}/{versionId}/savePlatformVersions", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void savePlatformVersions(@PathVariable final long projectId, @PathVariable final long versionId, @RequestBody @Valid final UpdatePlatformVersions updatePlatformVersions) {
        this.versionDependencyService.updateVersionPlatformVersions(projectId, versionId, updatePlatformVersions);
    }

    @Unlocked
    @ResponseStatus(HttpStatus.OK)
    @PermissionRequired(type = PermissionType.PROJECT, perms = NamedPermission.EDIT_VERSION, args = "{#projectId}")
    @RateLimit(overdraft = 5, refillTokens = 1, refillSeconds = 10)
    @PostMapping(path = "/version/{projectId}/{versionId}/savePluginDependencies", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void savePluginDependencies(@PathVariable final long projectId, @PathVariable final long versionId, @RequestBody @Valid final UpdatePluginDependencies updatePluginDependencies) {
        this.versionDependencyService.updateVersionPluginDependencies(projectId, versionId, updatePluginDependencies);
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
    public void softDeleteVersion(@PathVariable final long projectId, @PathVariable("versionId") final ProjectVersionTable version, @RequestBody @Valid final StringContent commentContent) {
        this.versionService.softDeleteVersion(projectId, version, commentContent.getContent());
    }

    @Unlocked
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PermissionRequired(NamedPermission.HARD_DELETE_VERSION)
    @PostMapping(path = "/version/{projectId}/{versionId}/hardDelete", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void hardDeleteVersion(@PathVariable("projectId") final ProjectTable projectTable, @PathVariable("versionId") final ProjectVersionTable projectVersionTable, @RequestBody @Valid final StringContent commentContent) {
        this.versionService.hardDeleteVersion(projectTable, projectVersionTable, commentContent.getContent());
    }

    @Unlocked
    @ResponseStatus(HttpStatus.CREATED)
    @PermissionRequired(NamedPermission.RESTORE_VERSION)
    @PostMapping("/version/{projectId}/{versionId}/restore")
    public void restoreVersion(@PathVariable final long projectId, @PathVariable("versionId") final ProjectVersionTable version) {
        this.versionService.restoreVersion(projectId, version);
    }

    @ResponseBody
    @RateLimit(overdraft = 5, refillTokens = 1, refillSeconds = 20)
    @VisibilityRequired(type = VisibilityRequired.Type.VERSION, args = "{#slug, #versionString, #platform}")
    // TODO is platform needed in the visibility check? it's not used in the VisibilityRequiredVoter
    @GetMapping(path = "/version/{slug}/versions/{versionString}/{platform}/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<?> download(@PathVariable final String slug, @PathVariable final String versionString, @PathVariable final Platform platform) {
        return this.downloadService.downloadVersion(slug, versionString, platform);
    }

    @Anyone
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RateLimit(overdraft = 5, refillTokens = 1, refillSeconds = 60)
    @GetMapping("/version/{versionId}/{platform}/track")
    public void trackDownload(@PathVariable final long versionId, @PathVariable final Platform platform) {
        this.versionService.trackDownload(versionId, platform);
    }
}
