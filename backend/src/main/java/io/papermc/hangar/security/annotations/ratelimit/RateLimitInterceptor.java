package io.papermc.hangar.security.annotations.ratelimit;

import io.github.bucket4j.Bucket;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.service.internal.BucketService;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(RateLimitInterceptor.class);
    private final BucketService bucketService;

    @Autowired
    public RateLimitInterceptor(final BucketService bucketService) {
        this.bucketService = bucketService;
    }

    @Override
    public boolean preHandle(final @NotNull HttpServletRequest request, final @NotNull HttpServletResponse response, final @NotNull Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        final Method method = handlerMethod.getMethod();
        final RateLimit limit = method.getAnnotation(RateLimit.class);
        if (limit != null) {
            this.applyLimit(request.getServletPath(), limit);
        }

        final RateLimit superLimit = method.getDeclaringClass().getAnnotation(RateLimit.class);
        if (superLimit != null) {
            this.applyLimit(request.getServletPath(), superLimit);
        }
        return true;
    }

    private void applyLimit(String path, final RateLimit limit) throws HangarApiException {
        if (!limit.path().isEmpty()) {
            path = limit.path();
        }

        final Bucket bucket = this.bucketService.bucket(path, limit);
        if (bucket != null && !bucket.tryConsume(1)) {
            LOGGER.debug("Applying rate limit for path {} due to limit at {}", path, limit.path());
            throw HangarApiException.rateLimited();
        }
    }
}
