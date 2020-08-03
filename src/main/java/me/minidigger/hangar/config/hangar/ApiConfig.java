package me.minidigger.hangar.config.hangar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.boot.convert.DurationUnit;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Component
@ConfigurationProperties(prefix = "hangar.api")
public class ApiConfig {

    @NestedConfigurationProperty
    public Session session;

    @Autowired
    public ApiConfig(Session session) {
        this.session = session;
    }

    @Component
    @ConfigurationProperties(prefix = "hangar.api.session")
    public static class Session {
        private String publicExpiration = "3h";
        @DurationUnit(ChronoUnit.DAYS)
        private Duration expiration = Duration.ofDays(14);
        private String checkInterval = "5m";

        public String getPublicExpiration() {
            return publicExpiration;
        }

        public void setPublicExpiration(String publicExpiration) {
            this.publicExpiration = publicExpiration;
        }

        public Duration getExpiration() {
            return expiration;
        }

        public void setExpiration(Duration expiration) {
            this.expiration = expiration;
        }

        public String getCheckInterval() {
            return checkInterval;
        }

        public void setCheckInterval(String checkInterval) {
            this.checkInterval = checkInterval;
        }
    }
}
