package io.papermc.hangar.config.hangar;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "hangar.discourse")
public record DiscourseConfig(
    @DefaultValue("false") boolean enabled,
    @DefaultValue("https://papermc.io/forums/") String url,
    String adminUser,
    String apiKey,
    int category,
    int categoryDeleted
) {
}
