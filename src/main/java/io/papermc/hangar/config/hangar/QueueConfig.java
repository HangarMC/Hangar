package io.papermc.hangar.config.hangar;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@ConfigurationProperties(prefix = "hangar.queue")
public class QueueConfig {
    private Duration maxReviewTime = Duration.ofDays(1);

    public Duration getMaxReviewTime() {
        return maxReviewTime;
    }

    public void setMaxReviewTime(Duration maxReviewTime) {
        this.maxReviewTime = maxReviewTime;
    }
}
