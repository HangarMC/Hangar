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
import java.net.InetAddress;
import java.time.Duration;
import java.util.Locale;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;

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
    public @Nullable Bucket bucket(final String path, final RateLimit limit) {
        // TODO local/loopback address checks/admin user check => return null?
        final InetAddress address = RequestUtil.getRemoteInetAddress(this.request);
        final Cache<String, Bucket> pathCache = this.cache.get(address);
        return pathCache.get(path.toLowerCase(Locale.ROOT), p -> this.createBucket(limit));
    }

    private Bucket createBucket(final RateLimit limit) {
        final Refill refill = limit.greedy() ? Refill.greedy(limit.refillTokens(), Duration.ofSeconds(limit.refillSeconds()))
            : Refill.intervally(limit.refillTokens(), Duration.ofSeconds(limit.refillSeconds()));
        final Bandwidth bandwidth = Bandwidth.classic(limit.overdraft(), refill);
        return Bucket.builder().addLimit(bandwidth).build();
    }
}
