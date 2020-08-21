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

    public Version getVersion(String pluginId, String versionString, boolean canSeeHidden, Long userId) {
        return apiVersionsDao.get().getVersion(pluginId, versionString, canSeeHidden, userId);
    }

    public List<Version> getVersionList(String pluginId, List<String> tags, boolean canSeeHidden, Long limit, long offset, Long userId) {
        return apiVersionsDao.get().listVersions(pluginId, tags, canSeeHidden, limit, offset, userId);
    }

    public long getVersionCount(String pluginId, List<String> tags, boolean canSeeHidden, Long userId) {
        Long count = apiVersionsDao.get().versionCount(pluginId, tags, canSeeHidden, userId);
        return count == null ? 0 : count;
    }

    public Map<String, VersionStatsDay> getVersionStats(String pluginId, String versionString, LocalDate fromDate, LocalDate toDate) {
        return apiVersionsDao.get().versionStats(pluginId, versionString, fromDate, toDate);
    }

}
