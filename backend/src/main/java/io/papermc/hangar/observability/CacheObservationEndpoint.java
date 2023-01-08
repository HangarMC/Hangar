package io.papermc.hangar.observability;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

@Component
@Endpoint(id = "cache-info")
public class CacheObservationEndpoint {

    private final CacheManager cacheManager;

    public CacheObservationEndpoint(final CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @ReadOperation
    public Map<String, Stats> getData() {
        final Map<String, Stats> stats = new HashMap<>();
        for (final String cacheName : this.cacheManager.getCacheNames()) {
            final org.springframework.cache.Cache wrapper = this.cacheManager.getCache(cacheName);
            if (wrapper.getNativeCache() instanceof Cache<?, ?> cache) {
                final CacheStats stat = cache.stats();
                stats.put(cacheName, new Stats(cache.estimatedSize(), stat.hitCount(), stat.missCount(), stat.loadSuccessCount(), stat.loadFailureCount(), stat.totalLoadTime(),stat.evictionCount(), stat.evictionWeight()));
            }
        }
        return stats;
    }

    record Stats(long size, long hitCount, long missCount, long loadSuccessCount, long loadFailureCount, long totalLoadTime, long evictionCount, long evictionWeight) {
    }
}
