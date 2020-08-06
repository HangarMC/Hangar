package me.minidigger.hangar.config.hangar;

import me.minidigger.hangar.HangarApplication;
import me.minidigger.hangar.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "hangar")
@ComponentScan("me.minidigger.hangar")
public class HangarConfig {

    private String logo = "https://paper.readthedocs.io/en/latest/_images/papermc_logomark_500.png";
    private String service = "Hangar";
    private List<Sponsor> sponsors;

    private boolean debug = false;
    private int debugLevel = 3;
    private boolean staging = true;
    private boolean logTimings = false;
    private String authUrl = "https://hangarauth.minidigger.me";
    private final ApplicationHome home = new ApplicationHome(HangarApplication.class);
    private String pluginUploadDir = home.getDir().toPath().resolve("work").toString();
    private String baseUrl = "https://localhost:8080";

    @NestedConfigurationProperty
    public final FakeUserConfig fakeUser;
    @NestedConfigurationProperty
    public HomepageConfig homepage;
    @NestedConfigurationProperty
    public ChannelsConfig channels;
    @NestedConfigurationProperty
    public PagesConfig pages;
    @NestedConfigurationProperty
    public ProjectsConfig projects;
    @NestedConfigurationProperty
    public UserConfig user;
    @NestedConfigurationProperty
    public OrgConfig org;
    @NestedConfigurationProperty
    public ApiConfig api;
    @NestedConfigurationProperty
    public SsoConfig sso;
    @NestedConfigurationProperty
    public HangarSecurityConfig security;
    @NestedConfigurationProperty
    public QueueConfig queue;

    @Component
    public static class Sponsor {
        private String name;
        private String image;
        private String link;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = image;
        }

        public String getLink() {
            return link;
        }

        public void setLink(String link) {
            this.link = link;
        }
    }

    @Autowired
    public HangarConfig(FakeUserConfig fakeUser, HomepageConfig homepage, ChannelsConfig channels, PagesConfig pages, ProjectsConfig projects, UserConfig user, OrgConfig org, ApiConfig api, SsoConfig sso, HangarSecurityConfig security, QueueConfig queue) {
        this.fakeUser = fakeUser;
        this.homepage = homepage;
        this.channels = channels;
        this.pages = pages;
        this.projects = projects;
        this.user = user;
        this.org = org;
        this.api = api;
        this.sso = sso;
        this.security = security;
        this.queue = queue;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public List<Sponsor> getSponsors() {
        return sponsors;
    }

    public void setSponsors(List<Sponsor> sponsors) {
        this.sponsors = sponsors;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public int getDebugLevel() {
        return debugLevel;
    }

    public void setDebugLevel(int debugLevel) {
        this.debugLevel = debugLevel;
    }

    public boolean isStaging() {
        return staging;
    }

    public void setStaging(boolean staging) {
        this.staging = staging;
    }

    public boolean isLogTimings() {
        return logTimings;
    }

    public void setLogTimings(boolean logTimings) {
        this.logTimings = logTimings;
    }

    public String getAuthUrl() {
        return authUrl;
    }

    public void setAuthUrl(String authUrl) {
        this.authUrl = authUrl;
    }

    public String getPluginUploadDir() {
        return pluginUploadDir;
    }

    public void setPluginUploadDir(String pluginUploadDir) {
        this.pluginUploadDir = pluginUploadDir;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void checkDebug() {
        if (!debug) {
            throw new UnsupportedOperationException("this function is supported in debug mode only");
        }
    }

    public boolean isValidProjectName(String name) {
        String sanitized = StringUtils.compact(name);
        return sanitized.length() >= 1 && sanitized.length() <= projects.getMaxNameLen();
    }

    // Added to make freemarker realize they are here
    public FakeUserConfig getFakeUser() {
        return fakeUser;
    }

    public HomepageConfig getHomepage() {
        return homepage;
    }

    public ChannelsConfig getChannels() {
        return channels;
    }

    public PagesConfig getPages() {
        return pages;
    }

    public ProjectsConfig getProjects() {
        return projects;
    }

    public UserConfig getUser() {
        return user;
    }

    public OrgConfig getOrg() {
        return org;
    }

    public ApiConfig getApi() {
        return api;
    }

    public SsoConfig getSso() {
        return sso;
    }

    public HangarSecurityConfig getSecurity() {
        return security;
    }

    public QueueConfig getQueue() {
        return queue;
    }
}
