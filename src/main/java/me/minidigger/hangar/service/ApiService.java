package me.minidigger.hangar.service;

import me.minidigger.hangar.db.dao.HangarDao;
import me.minidigger.hangar.db.dao.api.ApiVersionsDao;
import me.minidigger.hangar.model.generated.Version;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ApiService {

    private final HangarDao<ApiVersionsDao> apiVersionsDao;

    public ApiService(HangarDao<ApiVersionsDao> apiVersionsDao) {
        this.apiVersionsDao = apiVersionsDao;
    }

    public Version getVersion(String pluginId, String versionString, boolean canSeeHidden, Long userId) {
        return apiVersionsDao.get().getVersion(pluginId, versionString, canSeeHidden, userId);
    }

    public List<Version> getVersionList(String pluginId, List<String> tags, boolean canSeeHidden, Long limit, long offset, Long userId) {
        return apiVersionsDao.get().listVersions(pluginId, tags, canSeeHidden, limit, offset, userId);
    }

    public long getVersionCount(String pluginId, List<String> tags, boolean canSeeHidden, Long userId) {
        return apiVersionsDao.get().versionCount(pluginId, tags, canSeeHidden, userId);
    }

}
