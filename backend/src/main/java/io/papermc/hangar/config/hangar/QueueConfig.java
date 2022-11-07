package io.papermc.hangar.config.hangar;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.boot.convert.DurationUnit;

@ConfigurationProperties(prefix = "hangar.queue")
public record QueueConfig(@DefaultValue("1") @DurationUnit(ChronoUnit.DAYS) Duration maxReviewTime) {
}
