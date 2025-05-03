package io.papermc.hangar.config.hangar;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "hangar.meili")
public record MeiliConfig(
    String url,
    String key,
    String prefix
) {
}
