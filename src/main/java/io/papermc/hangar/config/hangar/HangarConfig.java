package io.papermc.hangar.config.hangar;

import io.papermc.hangar.HangarApplication;
import io.papermc.hangar.model.Announcement;
import io.papermc.hangar.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "hangar")
@ComponentScan("io.papermc.hangar")
public class HangarConfig {

    private String logo = "https://paper.readthedocs.io/en/latest/_images/papermc_logomark_500.png";
    private String service = "Hangar";
    private List<Sponsor> sponsors;

    private boolean dev = true;
    private String authUrl;
    private String pluginUploadDir = new ApplicationHome(HangarApplication.class).getDir().toPath().resolve("work").toString();
    private String baseUrl;
    private String gaCode = "";
    private List<Announcement> announcements = new ArrayList<>();
    private String urlRegex = "^(https?:\\/\\/(?:www\\.|(?!www))[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\\.[^\\s]{2,}|www\\.[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\\.[^\\s]{2,}|https?:\\/\\/(?:www\\.|(?!www))[a-zA-Z0-9]+\\.[^\\s]{2,}|www\\.[a-zA-Z0-9]+\\.[^\\s]{2,})";
    private List<String> licences = new ArrayList<>();

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
    public OrganizationsConfig org;
    @NestedConfigurationProperty
    public ApiConfig api;
    @NestedConfigurationProperty
    public SSOConfig sso;
    @NestedConfigurationProperty
    public HangarSecurityConfig security;
    @NestedConfigurationProperty
    public QueueConfig queue;
    @NestedConfigurationProperty
    public DiscourseConfig discourse;

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
    public HangarConfig(FakeUserConfig fakeUser, HomepageConfig homepage, ChannelsConfig channels, PagesConfig pages, ProjectsConfig projects, UserConfig user, OrganizationsConfig org, ApiConfig api, SSOConfig sso, HangarSecurityConfig security, QueueConfig queue, DiscourseConfig discourse) {
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
        this.discourse = discourse;
    }

    public void checkDev() {
        if (!this.dev) {
            throw new UnsupportedOperationException("Only supported in dev mode!");
        }
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

    public List<Announcement> getAnnouncements() {
        return announcements;
    }

    public void setAnnouncements(List<Announcement> announcements) {
        this.announcements = announcements;
    }

    public boolean isDev() {
        return dev;
    }

    public void setDev(boolean dev) {
        this.dev = dev;
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

    public String getGaCode() {
        return gaCode;
    }

    public void setGaCode(String gaCode) {
        this.gaCode = gaCode;
    }

    public String getUrlRegex() {
        return urlRegex;
    }

    public void setUrlRegex(String urlRegex) {
        this.urlRegex = urlRegex;
    }

    public List<String> getLicences() {
        return licences;
    }

    public void setLicences(List<String> licences) {
        this.licences = licences;
    }

    @Deprecated(forRemoval = true)
    public boolean isValidProjectName(String name) {
        String sanitized = StringUtils.compact(name);
        return sanitized.length() >= 1 && sanitized.length() <= projects.getMaxNameLen() && projects.getNameMatcher().test(name);
    }

    // Added to make freemarker realize they are here
    // TODO remove cause they are only needed for freemarker
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

    public OrganizationsConfig getOrg() {
        return org;
    }

    public ApiConfig getApi() {
        return api;
    }

    public SSOConfig getSso() {
        return sso;
    }

    public HangarSecurityConfig getSecurity() {
        return security;
    }

    public QueueConfig getQueue() {
        return queue;
    }

    public DiscourseConfig getDiscourse() {
        return discourse;
    }
}
