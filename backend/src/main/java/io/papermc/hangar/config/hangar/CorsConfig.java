package io.papermc.hangar.config.hangar;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Set;

@ConfigurationProperties(prefix = "hangar.cors")
public record CorsConfig(
        Set<String> allowedHosts
) {
}
