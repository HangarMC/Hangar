package io.papermc.hangar.controller.api.v1;

import io.papermc.hangar.controller.api.v1.interfaces.IVersionsController;
import io.papermc.hangar.controller.extras.pagination.annotations.ApplicableFilters;
import io.papermc.hangar.controller.extras.pagination.filters.versions.VersionChannelFilter;
import io.papermc.hangar.controller.extras.pagination.filters.versions.VersionPlatformFilter;
import io.papermc.hangar.controller.extras.pagination.filters.versions.VersionTagFilter;
import io.papermc.hangar.model.api.PaginatedResult;
import io.papermc.hangar.model.api.project.version.Version;
import io.papermc.hangar.model.api.project.version.VersionStats;
import io.papermc.hangar.model.api.requests.RequestPagination;
import io.papermc.hangar.model.common.NamedPermission;
import io.papermc.hangar.model.common.PermissionType;
import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.security.annotations.Anyone;
import io.papermc.hangar.security.annotations.permission.PermissionRequired;
import io.papermc.hangar.security.annotations.ratelimit.RateLimit;
import io.papermc.hangar.security.annotations.visibility.VisibilityRequired;
import io.papermc.hangar.security.annotations.visibility.VisibilityRequired.Type;
import io.papermc.hangar.service.api.VersionsApiService;
import io.papermc.hangar.service.internal.versions.DownloadService;
import java.time.OffsetDateTime;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ResponseBody;

@Anyone
@Controller
@ResponseBody
@RateLimit(path = "apiversions", overdraft = 200, refillTokens = 50, greedy = true)
public class VersionsController implements IVersionsController {

    private final DownloadService downloadService;
    private final VersionsApiService versionsApiService;

    @Autowired
    public VersionsController(DownloadService downloadService, VersionsApiService versionsApiService) {
        this.downloadService = downloadService;
        this.versionsApiService = versionsApiService;
    }

    @Override
    @VisibilityRequired(type = Type.PROJECT, args = "{#author, #slug}")
    public Version getVersion(String author, String slug, String name) {
        return versionsApiService.getVersion(author, slug, name);
    }

    @Override
    @VisibilityRequired(type = Type.PROJECT, args = "{#author, #slug}")
    @ApplicableFilters({VersionChannelFilter.class, VersionPlatformFilter.class, VersionTagFilter.class})
    public PaginatedResult<Version> getVersions(String author, String slug, @NotNull RequestPagination pagination) {
        return versionsApiService.getVersions(author, slug, pagination);
    }

    @Override
    @PermissionRequired(type = PermissionType.PROJECT, perms = NamedPermission.IS_SUBJECT_MEMBER, args = "{#author, #slug}")
    public Map<String, VersionStats> getVersionStats(String author, String slug, String versionString, Platform platform, @NotNull OffsetDateTime fromDate, @NotNull OffsetDateTime toDate) {
        return versionsApiService.getVersionStats(author, slug, versionString, platform, fromDate, toDate);
    }

    @Override
    @RateLimit(overdraft = 10, refillTokens = 2)
    @VisibilityRequired(type = Type.VERSION, args = "{#author, #slug, #versionString, #platform}")
    public Resource downloadVersion(String author, String slug, String versionString, Platform platform) {
        return downloadService.getVersionFile(author, slug, versionString, platform, false, null);
    }
}
