package io.papermc.hangar.service.api;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.db.dao.v1.VersionsApiDAO;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.api.PaginatedResult;
import io.papermc.hangar.model.api.Pagination;
import io.papermc.hangar.model.api.project.Project;
import io.papermc.hangar.model.api.project.version.UploadedVersion;
import io.papermc.hangar.model.api.project.version.Version;
import io.papermc.hangar.model.api.project.version.VersionStats;
import io.papermc.hangar.model.api.requests.RequestPagination;
import io.papermc.hangar.model.common.Permission;
import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.model.db.projects.ProjectTable;
import io.papermc.hangar.model.internal.versions.PendingVersion;
import io.papermc.hangar.model.internal.versions.VersionUpload;
import io.papermc.hangar.service.internal.PlatformService;
import io.papermc.hangar.service.internal.projects.ProjectService;
import io.papermc.hangar.service.internal.versions.VersionDependencyService;
import io.papermc.hangar.service.internal.versions.VersionFactory;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class VersionsApiService extends HangarComponent {

    private final VersionsApiDAO versionsApiDAO;
    private final VersionDependencyService versionDependencyService;
    private final VersionFactory versionFactory;
    private final ProjectsApiService projectsApiService;
    private final PlatformService platformService;
    private final ProjectService projectService;

    @Autowired
    public VersionsApiService(final VersionsApiDAO versionsApiDAO, final VersionDependencyService versionDependencyService, final VersionFactory versionFactory, final ProjectsApiService projectsApiService, final PlatformService platformService, final ProjectService projectService) {
        this.versionsApiDAO = versionsApiDAO;
        this.versionDependencyService = versionDependencyService;
        this.versionFactory = versionFactory;
        this.projectsApiService = projectsApiService;
        this.platformService = platformService;
        this.projectService = projectService;
    }

    @Transactional
    public UploadedVersion uploadVersion(final String slug, final List<MultipartFile> files, final VersionUpload versionUpload) {
        final Project project = this.projectsApiService.getProject(slug);
        if (project == null) {
            throw new HangarApiException(HttpStatus.NOT_FOUND, "Project not found");
        }

        this.matchVersionRanges(versionUpload.getPlatformDependencies());

        // TODO Do the upload in one step
        final PendingVersion preparedPendingVersion = this.versionFactory.createPendingVersion(project.getId(), versionUpload.getFiles(), files, versionUpload.getChannel(), false);
        final PendingVersion pendingVersion = versionUpload.toPendingVersion(preparedPendingVersion.getFiles());
        this.versionFactory.publishPendingVersion(project.getId(), pendingVersion);
        return new UploadedVersion(this.config.getBaseUrl() + "/" + project.getNamespace().toString() + "/versions/" + pendingVersion.getVersionString());
    }

    private void matchVersionRanges(final Map<Platform, SortedSet<String>> versions) {
        for (final Map.Entry<Platform, SortedSet<String>> entry : versions.entrySet()) {
            final Platform platform = entry.getKey();
            final List<String> fullPlatformVersions = this.platformService.getFullVersionsForPlatform(platform);
            final SortedSet<String> selectedVersions = entry.getValue();
            if (selectedVersions.size() > fullPlatformVersions.size()) {
                throw new HangarApiException("Too many versions selected for platform " + platform.getName());
            }

            final Map<String, Integer> platformVersionIndexes = new HashMap<>();
            for (int i = 0; i < fullPlatformVersions.size(); i++) {
                platformVersionIndexes.put(fullPlatformVersions.get(i), i);
            }

            final Set<String> toAdd = new HashSet<>();
            selectedVersions.removeIf(selectedVersion -> this.match(toAdd, selectedVersion, platformVersionIndexes, fullPlatformVersions));
            for (final String version : toAdd) {
                if (!selectedVersions.add(version)) {
                    throw new HangarApiException("Duplicate version: " + version);
                }
            }
        }
    }

    private boolean match(final Set<String> toAdd, final String selectedVersion, final Map<String, Integer> platformVersionIndexes, final List<String> fullPlatformVersions) {
        final Integer index = platformVersionIndexes.get(selectedVersion);
        if (index != null) {
            return false;
        }

        final int separatorIndex = selectedVersion.indexOf('-');
        final boolean wildcard = selectedVersion.length() > 3 && selectedVersion.endsWith(".x");
        if (separatorIndex != -1 && wildcard) {
            throw new HangarApiException("Can't use wildcard and separator at the same time: " + selectedVersion);
        }

        if (separatorIndex != -1) {
            // e.g. 1.15-1.16.4
            final String[] split = selectedVersion.split("-");
            if (split.length != 2) {
                throw new HangarApiException("Invalid version range: " + selectedVersion);
            }

            final String start = split[0];
            final String end = split[1];
            final Integer startIndex = platformVersionIndexes.get(start);
            final Integer endIndex = platformVersionIndexes.get(end);
            if (startIndex == null || endIndex == null) {
                throw new HangarApiException("Start or end version do not exist in range: " + selectedVersion);
            }
            if (startIndex > endIndex) {
                throw new HangarApiException("Start version higher than end version in range: " + selectedVersion);
            }

            for (int i = startIndex; i <= endIndex; i++) {
                final String version = fullPlatformVersions.get(i);
                if (!toAdd.add(version)) {
                    throw new HangarApiException("Duplicate version in range: " + selectedVersion + " (" + version + ")");
                }
            }
        } else if (wildcard) {
            // e.g. 1.19.x
            final String parentVersion = selectedVersion.substring(0, selectedVersion.length() - 2);
            final Integer parentVersionIndex = platformVersionIndexes.get(parentVersion);
            if (parentVersionIndex == null) {
                throw new HangarApiException("Invalid version: " + selectedVersion);
            }

            for (int i = parentVersionIndex; i < fullPlatformVersions.size(); i++) {
                final String version = fullPlatformVersions.get(i);
                if (!version.startsWith(parentVersion)) {
                    break;
                }
                if (!toAdd.add(version)) {
                    throw new HangarApiException("Duplicate version in range: " + selectedVersion + " (" + version + ")");
                }
            }
        } else {
            throw new HangarApiException("Invalid version: " + selectedVersion);
        }
        return true;
    }

    @Transactional(readOnly = true)
    public Version getVersion(final String slug, final String versionString) {
        final Map.Entry<Long, Version> version = this.versionsApiDAO.getVersionWithVersionString(slug, versionString, this.getGlobalPermissions().has(Permission.SeeHidden), this.getHangarUserId());
        if (version == null) {
            throw new HangarApiException(HttpStatus.NOT_FOUND);
        }
        return this.versionDependencyService.addDownloadsAndDependencies(slug, versionString, version.getKey()).applyTo(version.getValue());
    }

    @Transactional(readOnly = true)
    public PaginatedResult<Version> getVersions(final String slug, final RequestPagination pagination) {
        //TODO Squash queries
        final boolean canSeeHidden = this.getGlobalPermissions().has(Permission.SeeHidden);
        final ProjectTable projectTable = this.projectService.getProjectTable(slug);
        if (projectTable == null) {
            throw new HangarApiException(HttpStatus.NOT_FOUND);
        }

        final List<Version> versions = this.versionsApiDAO.getVersions(slug, canSeeHidden, this.getHangarUserId(), pagination).entrySet().parallelStream()
            .map(entry -> this.versionDependencyService.addDownloadsAndDependencies(projectTable.getOwnerName(), slug, entry.getValue().getName(), entry.getKey()).applyTo(entry.getValue()))
            .sorted((v1, v2) -> v2.getCreatedAt().compareTo(v1.getCreatedAt()))
            .toList();
        final Long versionCount = this.versionsApiDAO.getVersionCount(slug, canSeeHidden, this.getHangarUserId(), pagination);
        return new PaginatedResult<>(new Pagination(versionCount == null ? 0 : versionCount, pagination), versions);
    }

    public Map<String, VersionStats> getVersionStats(final String slug, final String versionString, final OffsetDateTime fromDate, final OffsetDateTime toDate) {
        if (fromDate.isAfter(toDate)) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "From date is after to date");
        }
        return this.versionsApiDAO.getVersionStats(slug, versionString, fromDate, toDate);
    }

    public @Nullable String latestVersion(final String slug) {
        return this.latestVersion(slug, this.config.channels.nameDefault());
    }

    public @Nullable String latestVersion(final String slug, final String channel) {
        final boolean canSeeHidden = this.getGlobalPermissions().has(Permission.SeeHidden);
        final String version = this.versionsApiDAO.getLatestVersion(slug, channel, canSeeHidden, this.getHangarUserId());
        if (version == null) {
            throw new HangarApiException(HttpStatus.NOT_FOUND, "No version found for " + slug + " on channel " + channel);
        }
        return version;
    }
}
