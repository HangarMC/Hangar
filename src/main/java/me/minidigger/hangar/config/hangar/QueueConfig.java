package me.minidigger.hangar.config.hangar;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "hangar.queue")
public class QueueConfig {
    private String maxReviewTime = "1d";

    public String getMaxReviewTime() {
        return maxReviewTime;
    }

    public void setMaxReviewTime(String maxReviewTime) {
        this.maxReviewTime = maxReviewTime;
    }
}
