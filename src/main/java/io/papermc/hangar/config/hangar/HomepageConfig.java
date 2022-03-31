package io.papermc.hangar.config.hangar;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@ConfigurationProperties(prefix = "hangar.homepage")
public class HomepageConfig {
    private Duration updateInterval = Duration.ofMinutes(10);

    public Duration getUpdateInterval() {
        return updateInterval;
    }

    public void setUpdateInterval(Duration updateInterval) {
        this.updateInterval = updateInterval;
    }
}
