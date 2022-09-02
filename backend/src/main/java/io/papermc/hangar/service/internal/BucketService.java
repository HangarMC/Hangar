package io.papermc.hangar.service.internal;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.security.annotations.ratelimit.RateLimit;
import io.papermc.hangar.util.RequestUtil;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.time.Duration;
import java.util.Locale;

@Service
public class BucketService extends HangarComponent {

    private static final Duration EXPIRY = Duration.ofMinutes(RateLimit.MAX_REFILL_DELAY);
    private final LoadingCache<InetAddress, Cache<String, Bucket>> cache = Caffeine.newBuilder().expireAfterAccess(EXPIRY).build(address -> Caffeine.newBuilder().expireAfterWrite(EXPIRY).build());

    /**
     * Returns the path's bucket, or null if no limit should be applied to the current user.
     *
     * @param limit rate limit
     * @return bucket, or null if no limit should be applied
     */
    public @Nullable Bucket bucket(String path, RateLimit limit) {
        //TODO local/loopback address checks/admin user check => return null?
        InetAddress address = RequestUtil.getRemoteInetAddress(request);
        Cache<String, Bucket> pathCache = cache.get(address);
        return pathCache.get(path.toLowerCase(Locale.ROOT), p -> createBucket(limit));
    }

    private Bucket createBucket(RateLimit limit) {
        Refill refill = limit.greedy() ? Refill.greedy(limit.refillTokens(), Duration.ofSeconds(limit.refillSeconds()))
            : Refill.intervally(limit.refillTokens(), Duration.ofSeconds(limit.refillSeconds()));
        Bandwidth bandwidth = Bandwidth.classic(limit.overdraft(), refill);
        return Bucket.builder().addLimit(bandwidth).build();
    }
}
