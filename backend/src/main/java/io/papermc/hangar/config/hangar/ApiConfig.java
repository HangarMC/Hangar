package io.papermc.hangar.config.hangar;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.boot.convert.DurationUnit;

@ConfigurationProperties(prefix = "hangar.api")
public record ApiConfig(@NestedConfigurationProperty Session session) { // TODO is this used anywhere now?

    // TODO is this used anywhere now?
    @ConfigurationProperties(prefix = "hangar.api.session")
    public record Session(
        @DurationUnit(ChronoUnit.HOURS) @DefaultValue("3") Duration publicExpiration,
        @DurationUnit(ChronoUnit.DAYS) @DefaultValue("14") Duration expiration
    ) {
    }
}
