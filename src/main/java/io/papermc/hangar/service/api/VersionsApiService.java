package io.papermc.hangar.service.api;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.db.dao.v1.VersionsApiDAO;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.api.PaginatedResult;
import io.papermc.hangar.model.api.Pagination;
import io.papermc.hangar.model.api.project.version.Version;
import io.papermc.hangar.model.api.project.version.VersionStats;
import io.papermc.hangar.model.api.requests.RequestPagination;
import io.papermc.hangar.model.common.Permission;
import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.service.internal.versions.VersionDependencyService;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class VersionsApiService extends HangarComponent {

    private final VersionsApiDAO versionsApiDAO;
    private final VersionDependencyService versionDependencyService;

    @Autowired
    public VersionsApiService(VersionsApiDAO versionsApiDAO, VersionDependencyService versionDependencyService) {
        this.versionsApiDAO = versionsApiDAO;
        this.versionDependencyService = versionDependencyService;
    }

    public Version getVersion(String author, String slug, String versionString) {
        final Map.Entry<Long, Version> version = versionsApiDAO.getVersionWithVersionString(author, slug, versionString, getGlobalPermissions().has(Permission.SeeHidden), getHangarUserId());
        if (version == null) {
            throw new HangarApiException(HttpStatus.NOT_FOUND);
        }
        versionDependencyService.addDownloadsAndDependencies(version.getKey(), version.getValue());
        return version.getValue();
    }

    public PaginatedResult<Version> getVersions(String author, String slug, RequestPagination pagination) {
        boolean canSeeHidden = getGlobalPermissions().has(Permission.SeeHidden);
        List<Version> versions = versionsApiDAO.getVersions(author, slug, canSeeHidden, getHangarUserId(), pagination).entrySet().stream()
            .map(entry -> versionDependencyService.addDownloadsAndDependencies(entry.getKey(), entry.getValue()))
            .sorted((v1, v2) -> v2.getCreatedAt().compareTo(v1.getCreatedAt()))
            .collect(Collectors.toList());
        Long versionCount = versionsApiDAO.getVersionCount(author, slug, canSeeHidden, getHangarUserId(), pagination);
        return new PaginatedResult<>(new Pagination(versionCount == null ? 0 : versionCount, pagination), versions);
    }

    public Map<String, VersionStats> getVersionStats(String author, String slug, String versionString, Platform platform, OffsetDateTime fromDate, OffsetDateTime toDate) {
        if (fromDate.isAfter(toDate)) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "From date is after to date");
        }
        return versionsApiDAO.getVersionStats(author, slug, versionString, platform, fromDate, toDate);
    }
}
