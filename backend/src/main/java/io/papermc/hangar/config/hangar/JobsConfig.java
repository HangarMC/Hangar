package io.papermc.hangar.config.hangar;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.boot.convert.DurationUnit;

@ConfigurationProperties(prefix = "hangar.jobs")
public record JobsConfig(
    @DurationUnit(ChronoUnit.MINUTES) @DefaultValue("1") Duration checkInterval,
    @DurationUnit(ChronoUnit.MINUTES) @DefaultValue("15") Duration unknownErrorTimeout,
    @DurationUnit(ChronoUnit.MINUTES) @DefaultValue("5") Duration statusErrorTimeout,
    @DurationUnit(ChronoUnit.MINUTES) @DefaultValue("2") Duration notAvailableTimeout,
    @DefaultValue("32") int maxConcurrentJobs
) {
}
