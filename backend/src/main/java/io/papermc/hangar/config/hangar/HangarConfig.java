package io.papermc.hangar.config.hangar;

import io.papermc.hangar.model.Announcement;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(prefix = "hangar")
public record HangarConfig(
    boolean dev,
    String baseUrl,
    List<Announcement> announcements,
    String urlRegex,
    List<String> licenses,
    boolean allowIndexing,
    boolean disableJGroups,
    boolean disableRateLimiting,

    @NestedConfigurationProperty
    UpdateTasksConfig updateTasks,
    @NestedConfigurationProperty
    ChannelsConfig channels,
    @NestedConfigurationProperty
    PagesConfig pages,
    @NestedConfigurationProperty
    ProjectsConfig projects,
    @NestedConfigurationProperty
    UserConfig user,
    @NestedConfigurationProperty
    OrganizationsConfig org,
    @NestedConfigurationProperty
    HangarSecurityConfig security,
    @NestedConfigurationProperty
    QueueConfig queue,
    @NestedConfigurationProperty
    JobsConfig jobs,
    @NestedConfigurationProperty
    StorageConfig storage,
    @NestedConfigurationProperty
    CorsConfig cors,
    @NestedConfigurationProperty
    ImageConfig image,
    @NestedConfigurationProperty
    MailConfig mail,
    @NestedConfigurationProperty
    E2EConfig e2e,
    @NestedConfigurationProperty
    MeiliConfig meili
) {
    public void checkDev() {
        if (!this.dev) {
            throw new UnsupportedOperationException("Only supported in dev mode!");
        }
    }
}
