package io.papermc.hangar.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String AUTHORS_CACHE = "AUTHORS_CACHE";
    public static final String STAFF_CACHE = "STAFF_CACHE";
    // TODO dont need these caches anymore
    public static final String PENDING_VERSION_CACHE = "PENDING_VERSION_CACHE";
    public static final String NEW_VERSION_CACHE = "NEW_VERSION_CACHE";

}
