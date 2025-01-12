package io.papermc.hangar.config.hangar;

import io.papermc.hangar.model.Announcement;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "hangar")
public class HangarConfig {

    private boolean dev = true;
    private String baseUrl;
    private List<Announcement> announcements = new ArrayList<>();
    private String urlRegex = "^(https?:\\/\\/(?:www\\.|(?!www))[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\\.[^\\s]{2,}|www\\.[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\\.[^\\s]{2,}|https?:\\/\\/(?:www\\.|(?!www))[a-zA-Z0-9]+\\.[^\\s]{2,}|www\\.[a-zA-Z0-9]+\\.[^\\s]{2,})";
    private List<String> licenses = new ArrayList<>();
    private boolean allowIndexing = true;
    private boolean disableJGroups = false;
    private boolean disableRateLimiting = false;

    @NestedConfigurationProperty
    public UpdateTasksConfig updateTasks;
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
    public HangarSecurityConfig security;
    @NestedConfigurationProperty
    public QueueConfig queue;
    @NestedConfigurationProperty
    public JobsConfig jobs;
    @NestedConfigurationProperty
    public StorageConfig storage;
    @NestedConfigurationProperty
    public CorsConfig cors;
    @NestedConfigurationProperty
    public ImageConfig image;
    @NestedConfigurationProperty
    public MailConfig mail;
    @NestedConfigurationProperty
    public E2EConfig e2e;

    @Autowired
    public HangarConfig(final UpdateTasksConfig updateTasks, final ChannelsConfig channels, final PagesConfig pages, final ProjectsConfig projects, final UserConfig user, final OrganizationsConfig org, final ApiConfig api, final HangarSecurityConfig security, final QueueConfig queue, final JobsConfig jobs, final StorageConfig storage, final CorsConfig cors, final ImageConfig image, final MailConfig mail, final E2EConfig e2e) {
        this.updateTasks = updateTasks;
        this.channels = channels;
        this.pages = pages;
        this.projects = projects;
        this.user = user;
        this.org = org;
        this.api = api;
        this.security = security;
        this.queue = queue;
        this.jobs = jobs;
        this.storage = storage;
        this.cors = cors;
        this.image = image;
        this.mail = mail;
        this.e2e = e2e;
    }

    public void checkDev() {
        if (!this.dev) {
            throw new UnsupportedOperationException("Only supported in dev mode!");
        }
    }

    public List<Announcement> getAnnouncements() {
        return this.announcements;
    }

    public void setAnnouncements(final List<Announcement> announcements) {
        this.announcements = announcements;
    }

    public boolean isDev() {
        return this.dev;
    }

    public void setDev(final boolean dev) {
        this.dev = dev;
    }

    public String getBaseUrl() {
        return this.baseUrl;
    }

    public void setBaseUrl(final String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getUrlRegex() {
        return this.urlRegex;
    }

    public void setUrlRegex(final String urlRegex) {
        this.urlRegex = urlRegex;
    }

    public List<String> getLicenses() {
        return this.licenses;
    }

    public void setLicenses(final List<String> licenses) {
        this.licenses = licenses;
    }

    public boolean isAllowIndexing() {
        return this.allowIndexing;
    }

    public void setAllowIndexing(final boolean allowIndexing) {
        this.allowIndexing = allowIndexing;
    }

    public boolean isDisableJGroups() {
        return this.disableJGroups;
    }

    public void setDisableJGroups(final boolean disableJGroups) {
        this.disableJGroups = disableJGroups;
    }

    public boolean isDisableRateLimiting() {
        return this.disableRateLimiting;
    }

    public void setDisableRateLimiting(final boolean disableRateLimiting) {
        this.disableRateLimiting = disableRateLimiting;
    }
}
