package io.papermc.hangar.service.api;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.components.index.MeiliService;
import io.papermc.hangar.components.index.MeiliService.SearchResult;
import io.papermc.hangar.config.CacheConfig;
import io.papermc.hangar.controller.extras.pagination.Filter;
import io.papermc.hangar.db.dao.v1.VersionsApiDAO;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.api.PaginatedResult;
import io.papermc.hangar.model.api.project.version.UploadedVersion;
import io.papermc.hangar.model.api.project.version.Version;
import io.papermc.hangar.model.api.project.version.VersionStats;
import io.papermc.hangar.model.api.requests.RequestPagination;
import io.papermc.hangar.model.common.Permission;
import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.model.db.projects.ProjectTable;
import io.papermc.hangar.model.db.versions.ProjectVersionTable;
import io.papermc.hangar.model.internal.versions.PendingVersion;
import io.papermc.hangar.model.internal.versions.VersionUpload;
import io.papermc.hangar.service.internal.PlatformService;
import io.papermc.hangar.service.internal.versions.VersionFactory;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class VersionsApiService extends HangarComponent {

    private final VersionsApiDAO versionsApiDAO;
    private final VersionFactory versionFactory;
    private final PlatformService platformService;
    private final MeiliService meiliService;

    @Autowired
    public VersionsApiService(final VersionsApiDAO versionsApiDAO, final VersionFactory versionFactory, final PlatformService platformService, final MeiliService meiliService) {
        this.versionsApiDAO = versionsApiDAO;
        this.versionFactory = versionFactory;
        this.platformService = platformService;
        this.meiliService = meiliService;
    }

    @Transactional
    public UploadedVersion uploadVersion(final ProjectTable project, final List<MultipartFile> files, final VersionUpload versionUpload) {
        this.matchVersionRanges(versionUpload.getPlatformDependencies());

        // TODO Do the upload in one step
        final PendingVersion preparedPendingVersion = this.versionFactory.createPendingVersion(project.getId(), versionUpload.getFiles(), files, versionUpload.getChannel(), false);
        final PendingVersion pendingVersion = versionUpload.toPendingVersion(preparedPendingVersion.getFiles());
        this.versionFactory.publishPendingVersion(project.getId(), pendingVersion);
        return new UploadedVersion(this.config.getBaseUrl() + "/" + project.getOwnerName() + "/" + project.getSlug() + "/versions/" + pendingVersion.getVersionString());
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

    public Version getVersion(final ProjectVersionTable pvt) {
        final Version version = this.versionsApiDAO.getVersion(pvt.getId(), this.getGlobalPermissions().has(Permission.SeeHidden), this.getHangarUserId());
        if (version == null) {
            throw new HangarApiException(HttpStatus.NOT_FOUND);
        }
        return version;
    }

    public PaginatedResult<Version> getVersions(final ProjectTable project, final RequestPagination pagination, final boolean includeHiddenChannels) {
        if (project == null) {
            throw new HangarApiException(HttpStatus.NOT_FOUND);
        }

        // TODO includeHiddenChannels <---
        if (!includeHiddenChannels) {
            // versions = versions.filter(version -> !version.getChannel().getFlags().contains(ChannelFlag.HIDE_BY_DEFAULT));
        }

        final StringBuilder filters = new StringBuilder();
        filters.append("projectId = ").append(project.getId());
        if (!pagination.getFilters().isEmpty()) {
            filters.append(" AND ");
        }
        for (final Filter.FilterInstance filterInstance : pagination.getFilters().values()) {
            filterInstance.createMeili(filters);
        }

        final List<String> sort = new ArrayList<>();
        pagination.getSorters().forEach((key, value) -> {
            StringBuilder sb = new StringBuilder();
            value.accept(sb);
            sort.add(sb.toString());
        });
        if (sort.isEmpty()) {
            sort.add("createdAt:desc");
        }

        final SearchResult<Version> result = this.meiliService.searchVersions("", filters.toString(), sort, pagination.getOffset(), pagination.getLimit());
        return result.asPaginatedResult();
    }

    public Map<String, VersionStats> getVersionStats(final ProjectVersionTable version, final OffsetDateTime fromDate, final OffsetDateTime toDate) {
        if (fromDate.isAfter(toDate)) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "From date is after to date");
        }
        return this.versionsApiDAO.getVersionStats(version.getProjectId(), version.getId(), fromDate, toDate);
    }

    @CacheEvict(value = CacheConfig.LATEST_VERSION)
    public void evictLatestRelease(final long projectId) {
    }

    @Cacheable(value = CacheConfig.LATEST_VERSION, key = "#project.getId()")
    public @Nullable String latestVersion(final ProjectTable project) {
        return this.latestVersion(project, this.config.channels.nameDefault());
    }

    @CacheEvict(value = CacheConfig.LATEST_VERSION, key = "{#id, #channel != null ? #channel.toLowerCase() : null}")
    public void evictLatest(final long id, final String channel) {
    }

    @Cacheable(value = CacheConfig.LATEST_VERSION, key = "{#project.getId(), #channel != null ? #channel.toLowerCase() : null}")
    public @Nullable String latestVersion(final ProjectTable project, final String channel) {
        final String version = this.versionsApiDAO.getLatestVersion(project.getId(), channel == null ? this.config.channels.nameDefault() : channel);
        if (version == null) {
            throw new HangarApiException(HttpStatus.NOT_FOUND, "No version found for " + project.getSlug() + " on channel " + channel);
        }
        return version;
    }
}
