package io.papermc.hangar.service.api;

import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.v1.VersionsApiDAO;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.Permission;
import io.papermc.hangar.model.api.PaginatedResult;
import io.papermc.hangar.model.api.Pagination;
import io.papermc.hangar.model.api.project.version.Version;
import io.papermc.hangar.model.api.project.version.VersionStats;
import io.papermc.hangar.model.api.requests.RequestPagination;
import io.papermc.hangar.service.HangarService;
import io.papermc.hangar.util.StringUtils;
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

    public Version getVersion(String author, String slug, String name) {
        long versionId = StringUtils.getVersionId(name, new HangarApiException(HttpStatus.BAD_REQUEST, "badly formatted version string"));
        return versionsApiDAO.getVersion(author, slug, versionId, hangarApiRequest.getGlobalPermissions().has(Permission.SeeHidden), hangarApiRequest.getUserId());
    }

    public PaginatedResult<Version> getVersions(String author, String slug, List<String> tags, RequestPagination pagination) {
        List<Version> versions = versionsApiDAO.getVersions(author, slug, tags, hangarApiRequest.getGlobalPermissions().has(Permission.SeeHidden), hangarApiRequest.getUserId(), pagination.getLimit(), pagination.getOffset());
        Long versionCount = versionsApiDAO.getVersionCount(author, slug, tags, hangarApiRequest.getGlobalPermissions().has(Permission.SeeHidden), hangarApiRequest.getUserId());
        return new PaginatedResult<>(new Pagination(versionCount == null ? 0 : versionCount.longValue(), pagination), versions);
    }

    public Map<String, VersionStats> getVersionStats(String author, String slug, String version, OffsetDateTime fromDate, OffsetDateTime toDate) {
        if (fromDate.isAfter(toDate)) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "From date is after to date");
        }
        long versionId = StringUtils.getVersionId(version, new HangarApiException(HttpStatus.BAD_REQUEST, "badly formatted version string"));
        return versionsApiDAO.getVersionStats(author, slug, versionId, fromDate, toDate);
    }
}
