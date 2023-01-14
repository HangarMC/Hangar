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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VersionsApiService extends HangarComponent {

    private final VersionsApiDAO versionsApiDAO;
    private final VersionDependencyService versionDependencyService;

    @Autowired
    public VersionsApiService(final VersionsApiDAO versionsApiDAO, final VersionDependencyService versionDependencyService) {
        this.versionsApiDAO = versionsApiDAO;
        this.versionDependencyService = versionDependencyService;
    }

    @Transactional
    public Version getVersion(final String author, final String slug, final String versionString) {
        final Map.Entry<Long, Version> version = this.versionsApiDAO.getVersionWithVersionString(author, slug, versionString, this.getGlobalPermissions().has(Permission.SeeHidden), this.getHangarUserId());
        if (version == null) {
            throw new HangarApiException(HttpStatus.NOT_FOUND);
        }
        return this.versionDependencyService.addDownloadsAndDependencies(author, slug, versionString, version.getKey()).applyTo(version.getValue());
    }

    @Transactional
    public PaginatedResult<Version> getVersions(final String author, final String slug, final RequestPagination pagination) {
        final boolean canSeeHidden = this.getGlobalPermissions().has(Permission.SeeHidden);
        final List<Version> versions = this.versionsApiDAO.getVersions(author, slug, canSeeHidden, this.getHangarUserId(), pagination).entrySet().stream()
            .map(entry -> this.versionDependencyService.addDownloadsAndDependencies(author, slug, entry.getValue().getName(), entry.getKey()).applyTo(entry.getValue()))
            .sorted((v1, v2) -> v2.getCreatedAt().compareTo(v1.getCreatedAt()))
            .toList();
        final Long versionCount = this.versionsApiDAO.getVersionCount(author, slug, canSeeHidden, this.getHangarUserId(), pagination);
        return new PaginatedResult<>(new Pagination(versionCount == null ? 0 : versionCount, pagination), versions);
    }

    public Map<String, VersionStats> getVersionStats(final String author, final String slug, final String versionString, final Platform platform, final OffsetDateTime fromDate, final OffsetDateTime toDate) {
        if (fromDate.isAfter(toDate)) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "From date is after to date");
        }
        return this.versionsApiDAO.getVersionStats(author, slug, versionString, platform, fromDate, toDate);
    }
}
