package io.papermc.hangar.config.hangar;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "hangar.discovery")
public record DiscoveryConfig(
    @DefaultValue("365") int maxAgeDays,
    @DefaultValue("2") int perBucket
) {
}
