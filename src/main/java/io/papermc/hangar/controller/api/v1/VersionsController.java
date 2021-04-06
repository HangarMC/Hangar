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
import io.papermc.hangar.security.annotations.visibility.VisibilityRequired;
import io.papermc.hangar.security.annotations.visibility.VisibilityRequired.Type;
import io.papermc.hangar.service.api.VersionsApiService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Anyone
@Controller
public class VersionsController implements IVersionsController {

    private final VersionsApiService versionsApiService;

    @Autowired
    public VersionsController(VersionsApiService versionsApiService) {
        this.versionsApiService = versionsApiService;
    }

    @Override
    @VisibilityRequired(type = Type.VERSION, args = "{#author, #slug, #versionString, #platform}")
    public ResponseEntity<Version> getVersion(String author, String slug, String versionString, Platform platform) {
        return ResponseEntity.ok(versionsApiService.getVersion(author, slug, versionString, platform));
    }

    @Override
    @VisibilityRequired(type = Type.PROJECT, args = "{#author, #slug}")
    public ResponseEntity<List<Version>> getVersions(String author, String slug, String name) {
        return ResponseEntity.ok(versionsApiService.getVersions(author, slug, name));
    }

    @Override
    @VisibilityRequired(type = Type.PROJECT, args = "{#author, #slug}")
    @ApplicableFilters({VersionChannelFilter.class, VersionPlatformFilter.class, VersionTagFilter.class})
    public ResponseEntity<PaginatedResult<Version>> getVersions(String author, String slug, @NotNull RequestPagination pagination) {
        return ResponseEntity.ok(versionsApiService.getVersions(author, slug, pagination));
    }

    @Override
    @PermissionRequired(type = PermissionType.PROJECT, perms = NamedPermission.IS_SUBJECT_MEMBER, args = "{#author, #slug}")
    public ResponseEntity<Map<String, VersionStats>> getVersionStats(String author, String slug, String versionString, Platform platform, @NotNull OffsetDateTime fromDate, @NotNull OffsetDateTime toDate) {
        return ResponseEntity.ok(versionsApiService.getVersionStats(author, slug, versionString, platform, fromDate, toDate));
    }
}
