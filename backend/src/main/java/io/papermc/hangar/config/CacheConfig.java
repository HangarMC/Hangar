package io.papermc.hangar.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String AUTHORS = "authors";
    public static final String STAFF = "staff";
    public static final String PLATFORMS = "platforms";
}
