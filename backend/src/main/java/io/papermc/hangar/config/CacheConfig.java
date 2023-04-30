package io.papermc.hangar.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import io.papermc.hangar.service.ReplicationService;
import io.papermc.hangar.util.CacheWrapper;
import jakarta.annotation.PostConstruct;
import java.time.Duration;
import org.springframework.boot.actuate.metrics.cache.CacheMetricsRegistrar;
import org.springframework.cache.Cache;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
public class CacheConfig {

    public static final String AUTHORS = "authors-cache";
    public static final String STAFF = "staff-cache";
    public static final String PLATFORMS = "platforms-cache";
    public static final String PLATFORMS_FULL = "platforms-full-cache";
    public static final String PROJECTS = "projects-cache";
    public static final String CATEGORIES = "categories-cache";
    public static final String PERMISSIONS = "permissions-cache";
    public static final String CHANNEL_COLORS = "channelColors-cache";
    public static final String FLAG_REASONS = "flagReasons-cache";
    public static final String SPONSOR = "sponsor-cache";
    public static final String ANNOUNCEMENTS = "announcements-cache";
    public static final String PROJECT_ROLES = "projectRoles-cache";
    public static final String GLOBAL_ROLES = "globalRoles-cache";
    public static final String ORG_ROLES = "orgRoles-cache";
    public static final String LICENSES = "licenses-cache";
    public static final String VISIBILITIES = "visibilities-cache";
    public static final String PROMPTS = "prompts-cache";
    public static final String VERSION_INFO = "version-info-cache";
    public static final String VALIDATIONS = "validations-cache";
    public static final String LOGGED_ACTIONS = "logged-actions-cache";
    public static final String INDEX_SITEMAP = "indexSitemap-cache";
    public static final String GLOBAL_SITEMAP = "globalSitemap-cache";
    public static final String USER_SITEMAP = "userSitemap-cache";
    public static final String AVATARS = "avatars-cache";
    public static final String USERNAME = "username-cache";
    public static final String VERSION_DEPENDENCIES = "version-dependencies-cache";

    private final CacheMetricsRegistrar cacheMetricsRegistrar;
    private final CaffeineCacheManager cacheManager;
    private final ReplicationService replicationService;

    public CacheConfig(@Lazy final CacheMetricsRegistrar cacheMetricsRegistrar, final ReplicationService replicationService) {
        this.cacheMetricsRegistrar = cacheMetricsRegistrar;
        this.replicationService = replicationService;
        this.cacheManager = new CaffeineCacheManager();
    }

    @Bean(STAFF)
    Cache staffCache() {
        return this.createCache(STAFF, Duration.ofHours(1), 10);
    }

    @Bean(AUTHORS)
    Cache authorsCache() {
        return this.createCache(AUTHORS, Duration.ofHours(1), 10);
    }

    @Bean(PLATFORMS)
    Cache platformsCache() {
        return this.createCache(PLATFORMS, Duration.ofHours(12), 100);
    }

    @Bean(PLATFORMS_FULL)
    Cache platformFullCache() {
        return this.createCache(PLATFORMS_FULL, Duration.ofHours(24), 10);
    }

    @Bean(PROJECTS)
    Cache projectsCache() {
        return this.createCache(PROJECTS, Duration.ofHours(1), 10);
    }

    @Bean(CATEGORIES)
    Cache categoriesCache() {
        return this.createCache(CATEGORIES, Duration.ofHours(1), 1);
    }

    @Bean(PERMISSIONS)
    Cache permissionsCache() {
        return this.createCache(PERMISSIONS, Duration.ofHours(1), 1);
    }

    @Bean(CHANNEL_COLORS)
    Cache channelColorsCache() {
        return this.createCache(CHANNEL_COLORS, Duration.ofHours(1), 1);
    }

    @Bean(FLAG_REASONS)
    Cache flagReasonsCache() {
        return this.createCache(FLAG_REASONS, Duration.ofHours(1), 1);
    }

    @Bean(SPONSOR)
    Cache sponsorCache() {
        return this.createCache(SPONSOR, Duration.ofHours(1), 1);
    }

    @Bean(ANNOUNCEMENTS)
    Cache announcementsCache() {
        return this.createCache(ANNOUNCEMENTS, Duration.ofHours(1), 1);
    }

    @Bean(PROJECT_ROLES)
    Cache projectRolesCache() {
        return this.createCache(PROJECT_ROLES, Duration.ofHours(1), 1);
    }

    @Bean(GLOBAL_ROLES)
    Cache globalRolesCache() {
        return this.createCache(GLOBAL_ROLES, Duration.ofHours(1), 1);
    }

    @Bean(ORG_ROLES)
    Cache orgRolesCache() {
        return this.createCache(ORG_ROLES, Duration.ofHours(1), 1);
    }

    @Bean(LICENSES)
    Cache licensesCache() {
        return this.createCache(LICENSES, Duration.ofHours(1), 1);
    }

    @Bean(VISIBILITIES)
    Cache visibilitiesCache() {
        return this.createCache(VISIBILITIES, Duration.ofHours(1), 1);
    }

    @Bean(PROMPTS)
    Cache promptsCache() {
        return this.createCache(PROMPTS, Duration.ofHours(1), 1);
    }

    @Bean(VERSION_INFO)
    Cache versionInfoCache() {
        return this.createCache(VERSION_INFO, Duration.ofHours(1), 1);
    }

    @Bean(VALIDATIONS)
    Cache validationsCache() {
        return this.createCache(VALIDATIONS, Duration.ofHours(1), 1);
    }

    @Bean(LOGGED_ACTIONS)
    Cache loggedActionsCache() {
        return this.createCache(LOGGED_ACTIONS, Duration.ofHours(1), 1);
    }

    @Bean(INDEX_SITEMAP)
    Cache indexSitemapCache() {
        return this.createCache(INDEX_SITEMAP, Duration.ofHours(1), 1);
    }

    @Bean(GLOBAL_SITEMAP)
    Cache globalSitemapCache() {
        return this.createCache(GLOBAL_SITEMAP, Duration.ofHours(1), 1);
    }

    @Bean(USER_SITEMAP)
    Cache userSitemapCache() {
        return this.createCache(USER_SITEMAP, Duration.ofHours(1), 20);
    }

    @Bean(AVATARS)
    Cache avatarsCache() {
        return this.createCache(AVATARS, Duration.ofMinutes(30), 200);
    }

    @Bean(USERNAME)
    Cache usernameCache() {
        return this.createCache(USERNAME, Duration.ofHours(2), 500);
    }

    @Bean(VERSION_DEPENDENCIES)
    Cache versionDependenciesCache() {
        return this.createCache(VERSION_DEPENDENCIES, Duration.ofMinutes(30), 200);
    }

    @PostConstruct
    public void init() {
        for (final String cacheName : this.cacheManager.getCacheNames()) {
            final Cache cache = this.cacheManager.getCache(cacheName);
            this.cacheMetricsRegistrar.bindCacheToRegistry(cache);
        }
    }

    @Bean
    public CaffeineCacheManager caffeineCacheManager() {
        return this.cacheManager;
    }

    private Cache createCache(final String name, final Duration ttl, final long maxSize) {
        final var caffineCache = Caffeine.newBuilder()
            .expireAfterWrite(ttl)
            .expireAfterAccess(ttl)
            .maximumSize(maxSize)
            .recordStats()
            .build();

        this.cacheManager.registerCustomCache(name, new CacheWrapper(name,caffineCache, this.replicationService));
        return this.cacheManager.getCache(name);
    }
}
