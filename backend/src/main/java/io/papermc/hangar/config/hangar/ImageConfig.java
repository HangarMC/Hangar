package io.papermc.hangar.config.hangar;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "hangar.image")
public record ImageConfig(float quality, int size) {
}
