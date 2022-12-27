package io.papermc.hangar.service.internal;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.config.CacheConfig;
import io.papermc.hangar.db.dao.internal.table.PlatformVersionDAO;
import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.model.db.PlatformVersionTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PlatformService extends HangarComponent {

    private final PlatformVersionDAO platformVersionDAO;

    @Autowired
    public PlatformService(final PlatformVersionDAO platformVersionDAO) {
        this.platformVersionDAO = platformVersionDAO;
    }

    public List<String> getVersionsForPlatform(final Platform platform) {
        return this.platformVersionDAO.getVersionsForPlatform(platform);
    }

    @Transactional
    @CacheEvict(value = CacheConfig.PLATFORMS, allEntries = true)
    public void updatePlatformVersions(final Map<Platform, List<String>> platformVersions) {
        platformVersions.forEach((platform, versions) -> {
            final Map<String, PlatformVersionTable> platformVersionTables = this.platformVersionDAO.getForPlatform(platform);
            final Map<String, PlatformVersionTable> toBeRemoved = new HashMap<>();
            final Map<String, PlatformVersionTable> toBeAdded = new HashMap<>();
            platformVersionTables.forEach((version, pvt) -> {
                if (!versions.contains(version)) {
                    toBeRemoved.put(version, pvt);
                }
            });
            versions.forEach(v -> {
                if (!platformVersionTables.containsKey(v)) {
                    toBeAdded.put(v, new PlatformVersionTable(platform, v));
                }
            });
            this.platformVersionDAO.deleteAll(toBeRemoved.values());
            this.platformVersionDAO.insertAll(toBeAdded.values());
        });
        // TODO user action logging
    }
}
