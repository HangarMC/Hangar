package io.papermc.hangar.config.hangar;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "fake-user")
public record FakeUserConfig(
    @DefaultValue("true") boolean enabled,
    @DefaultValue("paper") String username,
    @DefaultValue("paper@papermc.io") String email
) {
}
