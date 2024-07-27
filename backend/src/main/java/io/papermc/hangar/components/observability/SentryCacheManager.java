package io.papermc.hangar.components.observability;

import org.jetbrains.annotations.NotNull;
import org.springframework.cache.Cache;
import org.springframework.cache.caffeine.CaffeineCacheManager;

public class SentryCacheManager extends CaffeineCacheManager {

    @Override
    public Cache getCache(@NotNull String name) {
        return new SentryCacheWrapper(super.getCache(name));
    }
}
