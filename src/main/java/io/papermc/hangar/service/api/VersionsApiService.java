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
import io.papermc.hangar.service.HangarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Service
public class VersionsApiService extends HangarService {

    private final VersionsApiDAO versionsApiDAO;

    @Autowired
    public VersionsApiService(HangarDao<VersionsApiDAO> versionsApiDAO) {
        this.versionsApiDAO = versionsApiDAO.get();
    }

    public Version getVersion(String author, String slug, String versionString, Platform platform) {
        return versionsApiDAO.getVersion(author, slug, versionString, platform, getGlobalPermissions().has(Permission.SeeHidden), getHangarUserId());
    }

    public List<Version> getVersions(String author, String slug, String versionString) {
        List<Version> versions = versionsApiDAO.getVersions(author, slug, versionString, getGlobalPermissions().has(Permission.SeeHidden), getHangarUserId());
        if (versions.isEmpty()) {
            throw new HangarApiException(HttpStatus.NOT_FOUND);
        }
        return versions;
    }

    public PaginatedResult<Version> getVersions(String author, String slug, List<String> tags, RequestPagination pagination) {
        List<Version> versions = versionsApiDAO.getVersions(author, slug, tags, getGlobalPermissions().has(Permission.SeeHidden), getHangarUserId(), pagination.getLimit(), pagination.getOffset());
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
