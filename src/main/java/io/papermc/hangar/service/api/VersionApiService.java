package io.papermc.hangar.service.api;

import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.api.VersionsApiDao;
import io.papermc.hangar.model.generated.Version;
import io.papermc.hangar.model.generated.VersionStatsDay;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class VersionApiService {

    private final HangarDao<VersionsApiDao> apiVersionsDao;

    public VersionApiService(HangarDao<VersionsApiDao> apiVersionsDao) {
        this.apiVersionsDao = apiVersionsDao;
    }

    public Version getVersion(String author, String slug, long versionId, boolean canSeeHidden, Long userId) {
        return apiVersionsDao.get().getVersion(author, slug, versionId, canSeeHidden, userId);
    }

    public List<Version> getVersionList(String author, String slug, List<String> tags, boolean canSeeHidden, Long limit, long offset, Long userId) {
        return apiVersionsDao.get().listVersions(author, slug, tags, canSeeHidden, limit, offset, userId);
    }

    public long getVersionCount(String author, String slug, List<String> tags, boolean canSeeHidden, Long userId) {
        Long count = apiVersionsDao.get().versionCount(author, slug, tags, canSeeHidden, userId);
        return count == null ? 0 : count;
    }

    public Map<String, VersionStatsDay> getVersionStats(String author, String slug, long versionId, LocalDate fromDate, LocalDate toDate) {
        return apiVersionsDao.get().versionStats(author, slug, versionId, fromDate, toDate);
    }

}
