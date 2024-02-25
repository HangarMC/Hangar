package io.papermc.hangar.config.hangar;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "hangar.e2e")
public record E2EConfig(
    String password,
    String totpSecret
) {
}
