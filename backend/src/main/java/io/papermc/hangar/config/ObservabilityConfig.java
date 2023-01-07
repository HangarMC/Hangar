package io.papermc.hangar.config;

import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.aop.ObservedAspect;
import io.papermc.hangar.observability.CacheableObservationAspect;
import io.papermc.hangar.observability.ObservabilitySqlLogger;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.spi.JdbiPlugin;
import org.jdbi.v3.core.statement.SqlLogger;
import org.springframework.boot.actuate.web.exchanges.InMemoryHttpExchangeRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration(proxyBeanMethods = false)
public class ObservabilityConfig {

    private final ObservationRegistry observationRegistry;

    public ObservabilityConfig(@Lazy final ObservationRegistry observationRegistry) {
        this.observationRegistry = observationRegistry;
    }

    @Bean
    public InMemoryHttpExchangeRepository inMemoryHttpExchangeRepository() {
        return new InMemoryHttpExchangeRepository();
    }

    @Bean
    public ObservedAspect observedAspect() {
        return new ObservedAspect(this.observationRegistry);
    }

    @Bean
    public CacheableObservationAspect cacheableObservationAspect() {
        return new CacheableObservationAspect(this.observationRegistry);
    }

    @Bean
    public JdbiPlugin observationPlugin() {
        return new JdbiPlugin() {
            @Override
            public void customizeJdbi(final Jdbi jdbi) {
                final SqlLogger myLogger = new ObservabilitySqlLogger(ObservabilityConfig.this.observationRegistry);
                jdbi.setSqlLogger(myLogger);
            }
        };
    }
}
