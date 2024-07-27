package io.papermc.hangar.components.observability;

import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.spi.JdbiPlugin;
import org.jdbi.v3.core.statement.SqlLogger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class SentryConfig {

    @Bean
    public SentryCacheableAspect cacheableObservationAspect() {
        return new SentryCacheableAspect();
    }

    @Bean
    public JdbiPlugin observationPlugin() {
        return new JdbiPlugin() {
            @Override
            public void customizeJdbi(final Jdbi jdbi) {
                final SqlLogger myLogger = new SentrySqlLogger();
                jdbi.setSqlLogger(myLogger);
            }
        };
    }
}
