package io.papermc.hangar.service.internal;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.config.CacheConfig;
import io.papermc.hangar.db.dao.internal.table.PlatformVersionDAO;
import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.model.api.platform.PlatformVersion;
import io.papermc.hangar.model.db.PlatformVersionTable;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.SequencedSet;
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
    private final PlatformVersionDAO platformVersionDAO;

    @Autowired
    public PlatformService(final PlatformVersionDAO platformVersionDAO) {
        this.platformVersionDAO = platformVersionDAO;
    }

    @Cacheable(CacheConfig.PLATFORMS_FULL)
    public List<String> getFullVersionsForPlatform(final Platform platform) {
        return this.platformVersionDAO.getVersionsForPlatform(platform);
    }

    public List<PlatformVersion> getDescendingVersionsForPlatform(final Platform platform) {
        final List<String> versions = this.platformVersionDAO.getVersionsForPlatform(platform);
        final Map<String, SequencedSet<String>> platformVersions = new LinkedHashMap<>();
        for (final String version : versions) {
            if (version.split("\\.").length <= 2) {
                platformVersions.put(version, new LinkedHashSet<>());
                continue;
            }

            final String parent = version.substring(0, version.lastIndexOf('.'));
            final SequencedSet<String> subVersions = platformVersions.get(parent);
            if (subVersions == null) {
                LOGGER.warn("Version {} does not have a parent version", version);
                platformVersions.put(version, new LinkedHashSet<>());
                continue;
            }

            subVersions.add(version);
        }

        // Reverse everything and add self
        for (final Map.Entry<String, SequencedSet<String>> entry : platformVersions.entrySet()) {
            final SequencedSet<String> subVersions = entry.getValue();
            if (!subVersions.isEmpty()) {
                final SequencedSet<String> reversed = subVersions.reversed();
                reversed.addLast(entry.getKey());
                entry.setValue(reversed);
            }
        }

        final List<PlatformVersion> list = platformVersions.entrySet().stream().map(entry -> new PlatformVersion(entry.getKey(), entry.getValue())).collect(Collectors.toList());
        Collections.reverse(list);
        return list;
    }

    @Transactional
    @CacheEvict(value = {CacheConfig.PLATFORMS_FULL}, allEntries = true)
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
            if (!toBeRemoved.isEmpty()) {
                this.platformVersionDAO.deleteAll(toBeRemoved.values());
            }
            if (!toBeAdded.isEmpty()) {
                this.platformVersionDAO.insertAll(toBeAdded.values());
            }
        });
        // TODO user action logging
    }
}
