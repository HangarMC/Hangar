package io.papermc.hangar.service.api;

import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.v1.VersionsApiDAO;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.api.PaginatedResult;
import io.papermc.hangar.model.api.Pagination;
import io.papermc.hangar.model.api.project.version.Version;
import io.papermc.hangar.model.api.project.version.VersionStats;
import io.papermc.hangar.model.api.requests.RequestPagination;
import io.papermc.hangar.model.common.Permission;
import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.model.db.versions.ProjectVersionTable;
import io.papermc.hangar.service.HangarService;
import io.papermc.hangar.service.internal.versions.VersionDependencyService;
import io.papermc.hangar.service.internal.versions.VersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class VersionsApiService extends HangarService {

    private final VersionsApiDAO versionsApiDAO;
    private final VersionService versionService;
    private final VersionDependencyService versionDependencyService;

    @Autowired
    public VersionsApiService(HangarDao<VersionsApiDAO> versionsApiDAO, VersionService versionService, VersionDependencyService versionDependencyService) {
        this.versionsApiDAO = versionsApiDAO.get();
        this.versionService = versionService;
        this.versionDependencyService = versionDependencyService;
    }

    public Version getVersion(String author, String slug, String versionString, Platform platform) {
        ProjectVersionTable projectVersionTable = versionService.getProjectVersionTable(author, slug, versionString, platform);
        if (projectVersionTable == null) {
            throw new HangarApiException(HttpStatus.NOT_FOUND);
        }
        var entry = versionsApiDAO.getVersion(projectVersionTable.getId(), getGlobalPermissions().has(Permission.SeeHidden), getHangarUserId());
        return versionDependencyService.addDependenciesAndTags(entry.getKey(), entry.getValue());
    }

    public List<Version> getVersions(String author, String slug, String versionString) {
        List<Version> versions = versionsApiDAO.getVersionsWithVersionString(author, slug, versionString, getGlobalPermissions().has(Permission.SeeHidden), getHangarUserId()).entrySet().stream().map(entry -> versionDependencyService.addDependenciesAndTags(entry.getKey(), entry.getValue())).collect(Collectors.toList());
        if (versions.isEmpty()) {
            throw new HangarApiException(HttpStatus.NOT_FOUND);
        }
        return versions;
    }

    public PaginatedResult<Version> getVersions(String author, String slug, List<String> tags, RequestPagination pagination) {
        List<Version> versions = versionsApiDAO.getVersions(author, slug, tags, getGlobalPermissions().has(Permission.SeeHidden), getHangarUserId(), pagination.getLimit(), pagination.getOffset()).entrySet().stream().map(entry -> versionDependencyService.addDependenciesAndTags(entry.getKey(), entry.getValue())).collect(Collectors.toList());
        Long versionCount = versionsApiDAO.getVersionCount(author, slug, tags, getGlobalPermissions().has(Permission.SeeHidden), getHangarUserId());
        return new PaginatedResult<>(new Pagination(versionCount == null ? 0 : versionCount, pagination), versions);
    }

    public Map<String, VersionStats> getVersionStats(String author, String slug, String versionString, Platform platform, OffsetDateTime fromDate, OffsetDateTime toDate) {
        if (fromDate.isAfter(toDate)) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "From date is after to date");
        }
        return versionsApiDAO.getVersionStats(author, slug, versionString, platform, fromDate, toDate);
    }
}
