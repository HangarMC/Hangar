package io.papermc.hangar.controller.api.v1;

import io.papermc.hangar.controller.api.v1.interfaces.IVersionsController;
import io.papermc.hangar.controller.extras.pagination.annotations.ApplicableFilters;
import io.papermc.hangar.controller.extras.pagination.annotations.ConfigurePagination;
import io.papermc.hangar.controller.extras.pagination.filters.versions.VersionChannelFilter;
import io.papermc.hangar.controller.extras.pagination.filters.versions.VersionPlatformFilter;
import io.papermc.hangar.controller.internal.config.VersionControllerConfig;
import io.papermc.hangar.model.api.PaginatedResult;
import io.papermc.hangar.model.api.project.version.UploadedVersion;
import io.papermc.hangar.model.api.project.version.Version;
import io.papermc.hangar.model.api.project.version.VersionStats;
import io.papermc.hangar.model.api.requests.RequestPagination;
import io.papermc.hangar.model.common.NamedPermission;
import io.papermc.hangar.model.common.PermissionType;
import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.model.db.projects.ProjectTable;
import io.papermc.hangar.model.db.versions.ProjectVersionTable;
import io.papermc.hangar.model.internal.versions.VersionUpload;
import io.papermc.hangar.security.annotations.Anyone;
import io.papermc.hangar.security.annotations.aal.RequireAal;
import io.papermc.hangar.security.annotations.permission.PermissionRequired;
import io.papermc.hangar.security.annotations.ratelimit.RateLimit;
import io.papermc.hangar.security.annotations.unlocked.Unlocked;
import io.papermc.hangar.security.annotations.visibility.VisibilityRequired;
import io.papermc.hangar.service.api.VersionsApiService;
import io.papermc.hangar.service.internal.versions.DownloadService;
import jakarta.servlet.http.HttpServletResponse;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

@Anyone
@Controller
@ResponseBody
@ServletComponentScan(basePackageClasses = VersionControllerConfig.class)
@RateLimit(path = "apiversions", greedy = true)
public class VersionsController implements IVersionsController {

    private final DownloadService downloadService;
    private final VersionsApiService versionsApiService;

    @Autowired
    public VersionsController(final DownloadService downloadService, final VersionsApiService versionsApiService) {
        this.downloadService = downloadService;
        this.versionsApiService = versionsApiService;
    }

    @Unlocked
    @RequireAal(1)
    @Override
    @RateLimit(overdraft = 5, refillTokens = 1, refillSeconds = 5)
    @PermissionRequired(type = PermissionType.PROJECT, perms = NamedPermission.CREATE_VERSION, args = "{#project}")
    public UploadedVersion uploadVersion(final ProjectTable project, final List<MultipartFile> files, final VersionUpload versionUpload) {
        return this.versionsApiService.uploadVersion(project, files, versionUpload);
    }

    @Override
    @VisibilityRequired(type = VisibilityRequired.Type.PROJECT, args = "{#project}")
    public Version getVersion(final ProjectTable project, final ProjectVersionTable version) {
        return this.versionsApiService.getVersion(project, version);
    }

    @Override
    @VisibilityRequired(type = VisibilityRequired.Type.VERSION, args = "{#version}")
    public Version getVersionById(final ProjectVersionTable version) {
        return this.versionsApiService.getVersion(version);
    }

    @Override
    @VisibilityRequired(type = VisibilityRequired.Type.PROJECT, args = "{#project}")
    @ApplicableFilters({VersionChannelFilter.class, VersionPlatformFilter.class})
    public PaginatedResult<Version> getVersions(final ProjectTable project,
                                                @ConfigurePagination(defaultLimitString = "@hangarConfig.projects.initVersionLoad", maxLimit = 25) final @NotNull RequestPagination pagination,
                                                @RequestParam(required = false, defaultValue = "true") final boolean includeHiddenChannels) {
        return this.versionsApiService.getVersions(project, pagination, includeHiddenChannels);
    }

    @Override
    @VisibilityRequired(type = VisibilityRequired.Type.PROJECT, args = "{#project}")
    public String getLatestReleaseVersion(final ProjectTable project) {
        return this.versionsApiService.latestVersion(project);
    }

    @Override
    @VisibilityRequired(type = VisibilityRequired.Type.PROJECT, args = "{#project}")
    public String getLatestVersion(final ProjectTable project, final @NotNull String channel) {
        return this.versionsApiService.latestVersion(project, channel);
    }

    @Override
    public Map<String, VersionStats> getVersionStats(final ProjectTable project, final ProjectVersionTable version, final @NotNull OffsetDateTime fromDate, final @NotNull OffsetDateTime toDate) {
        return this.versionsApiService.getVersionStats(version, fromDate, toDate);
    }

    @Override
    public Map<String, VersionStats> getVersionStatsById(final ProjectVersionTable version, final @NotNull OffsetDateTime fromDate, final @NotNull OffsetDateTime toDate) {
        return this.versionsApiService.getVersionStats(version, fromDate, toDate);
    }

    @Override
    @RateLimit(overdraft = 10, refillTokens = 2)
    @VisibilityRequired(type = VisibilityRequired.Type.VERSION, args = "{#project, #version, #platform}")
    public ResponseEntity<?> downloadVersion(final ProjectTable project, final ProjectVersionTable version, final Platform platform, final HttpServletResponse response) {
        return this.downloadService.downloadVersion(project, version, platform);
    }

    @Override
    @RateLimit(overdraft = 10, refillTokens = 2)
    @VisibilityRequired(type = VisibilityRequired.Type.VERSION, args = "{#version}")
    public ResponseEntity<?> downloadVersionById(final ProjectVersionTable version, final Platform platform, final HttpServletResponse response) {
        return this.downloadService.downloadVersion(version, platform);
    }
}
