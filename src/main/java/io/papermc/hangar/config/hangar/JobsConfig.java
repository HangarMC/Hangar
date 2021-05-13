package io.papermc.hangar.config.hangar;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Component
@ConfigurationProperties(prefix = "hangar.jobs")
public class JobsConfig {

    @DurationUnit(ChronoUnit.MINUTES)
    private Duration checkInterval = Duration.ofMinutes(1);

    @DurationUnit(ChronoUnit.MINUTES)
    private Duration unknownErrorTimeout = Duration.ofMinutes(15);

    @DurationUnit(ChronoUnit.MINUTES)
    private Duration statusErrorTimeout = Duration.ofMinutes(5);

    @DurationUnit(ChronoUnit.MINUTES)
    private Duration notAvailableTimeout = Duration.ofMinutes(2);

    private int maxConcurrentJobs = 32;

    public Duration getCheckInterval() {
        return checkInterval;
    }

    public void setCheckInterval(Duration checkInterval) {
        this.checkInterval = checkInterval;
    }

    public Duration getUnknownErrorTimeout() {
        return unknownErrorTimeout;
    }

    public void setUnknownErrorTimeout(Duration unknownErrorTimeout) {
        this.unknownErrorTimeout = unknownErrorTimeout;
    }

    public Duration getStatusErrorTimeout() {
        return statusErrorTimeout;
    }

    public void setStatusErrorTimeout(Duration statusErrorTimeout) {
        this.statusErrorTimeout = statusErrorTimeout;
    }

    public Duration getNotAvailableTimeout() {
        return notAvailableTimeout;
    }

    public void setNotAvailableTimeout(Duration notAvailableTimeout) {
        this.notAvailableTimeout = notAvailableTimeout;
    }

    public int getMaxConcurrentJobs() {
        return maxConcurrentJobs;
    }

    public void setMaxConcurrentJobs(int maxConcurrentJobs) {
        this.maxConcurrentJobs = maxConcurrentJobs;
    }
}
