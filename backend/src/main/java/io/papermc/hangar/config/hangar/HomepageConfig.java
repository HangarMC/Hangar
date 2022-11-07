package io.papermc.hangar.config.hangar;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.boot.convert.DurationUnit;

@ConfigurationProperties(prefix = "hangar.homepage")
public record HomepageConfig(@DurationUnit(ChronoUnit.MINUTES) @DefaultValue("10") Duration updateInterval) {
}
