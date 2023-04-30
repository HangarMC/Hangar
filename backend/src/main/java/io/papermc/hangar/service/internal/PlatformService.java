package io.papermc.hangar.service.internal;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.config.CacheConfig;
import io.papermc.hangar.db.dao.internal.table.PlatformVersionDAO;
import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.model.common.PlatformVersion;
import io.papermc.hangar.model.db.PlatformVersionTable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlatformService extends HangarComponent {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlatformService.class);
    private static final String[] ARR = new String[0];
    private final PlatformVersionDAO platformVersionDAO;

    @Autowired
    public PlatformService(final PlatformVersionDAO platformVersionDAO) {
        this.platformVersionDAO = platformVersionDAO;
    }

    @Cacheable(CacheConfig.PLATFORMS_FULL)
    public List<String> getFullVersionsForPlatform(final Platform platform) {
        return this.platformVersionDAO.getVersionsForPlatform(platform);
    }

    @Cacheable(CacheConfig.PLATFORMS)
    public List<PlatformVersion> getDescendingVersionsForPlatform(final Platform platform) {
        final List<String> versions = this.platformVersionDAO.getVersionsForPlatform(platform);
        final Map<String, List<String>> platformVersions = new LinkedHashMap<>();
        for (final String version : versions) {
            if (version.split("\\.").length <= 2) {
                platformVersions.put(version, new ArrayList<>());
                continue;
            }

            final String parent = version.substring(0, version.lastIndexOf('.'));
            final List<String> subVersions = platformVersions.get(parent);
            if (subVersions == null) {
                LOGGER.warn("Version {} does not have a parent version", version);
                platformVersions.put(version, new ArrayList<>());
                continue;
            }

            subVersions.add(version);
        }

        // Reverse everything and add self
        for (final Map.Entry<String, List<String>> entry : platformVersions.entrySet()) {
            final List<String> subVersions = entry.getValue();
            if (!subVersions.isEmpty()) {
                Collections.reverse(subVersions);
                subVersions.add(entry.getKey());
            }
        }

        final List<PlatformVersion> list = platformVersions.entrySet().stream().map(entry -> new PlatformVersion(entry.getKey(), entry.getValue().toArray(ARR))).collect(Collectors.toList());
        Collections.reverse(list);
        return list;
    }

    @Transactional
    @CacheEvict(value = {CacheConfig.PLATFORMS, CacheConfig.PLATFORMS_FULL}, allEntries = true)
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
            versions.forEach(version -> {
                if (!platformVersionTables.containsKey(version)) {
                    toBeAdded.put(version, new PlatformVersionTable(platform, version));
                }
            });
            this.platformVersionDAO.deleteAll(toBeRemoved.values());
            this.platformVersionDAO.insertAll(toBeAdded.values());
        });
        // TODO user action logging
    }

    @Cacheable(CacheConfig.PLATFORMS)
    public PlatformVersionTable getByPlatformAndVersion(final Platform platform, final String version) {
        return this.platformVersionDAO.getByPlatformAndVersion(platform, version);
    }
}
