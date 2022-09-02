package io.papermc.hangar.config.hangar;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Component
@ConfigurationProperties(prefix = "hangar.queue")
public class QueueConfig {
    @DurationUnit(ChronoUnit.DAYS)
    private Duration maxReviewTime = Duration.ofDays(1);

    public Duration getMaxReviewTime() {
        return maxReviewTime;
    }

    public void setMaxReviewTime(Duration maxReviewTime) {
        this.maxReviewTime = maxReviewTime;
    }
}
