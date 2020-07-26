package me.minidigger.hangar.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String AUTHORS_CACHE = "AUTHORS_CACHE";
    public static final String STAFF_CACHE = "STAFF_CACHE";

}
