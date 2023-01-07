package io.papermc.hangar.config;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.aop.ObservedAspect;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.time.temporal.ChronoUnit;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.spi.JdbiPlugin;
import org.jdbi.v3.core.statement.SqlLogger;
import org.jdbi.v3.core.statement.StatementContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.web.exchanges.InMemoryHttpExchangeRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import static io.micrometer.observation.Observation.createNotStarted;

@Configuration(proxyBeanMethods = false)
public class ManagementConfig {

    private static final Logger sqlLog = LoggerFactory.getLogger("sql");

    private final ObservationRegistry observationRegistry;

    private final boolean logSql = false; // for debugging sql statements

    public ManagementConfig(@Lazy final ObservationRegistry observationRegistry) {
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
    public JdbiPlugin observationPlugin() {
        return new JdbiPlugin() {
            @Override
            public void customizeJdbi(final Jdbi jdbi) {
                final SqlLogger myLogger = new SqlLogger() {
                    @Override
                    public void logBeforeExecution(final StatementContext context) {
                        if (ManagementConfig.this.logSql) {
                            sqlLog.info("sql be: " + context.getRenderedSql());
                        }

                        final Observation observation = createNotStarted(this.getObservationName(context), ManagementConfig.this.observationRegistry);
                        observation.start();
                        observation.highCardinalityKeyValue("hangar.sql.rendered", context.getRenderedSql());
                        observation.highCardinalityKeyValue("hangar.sql.binding", context.getBinding().toString());
                        context.define("observation", observation);
                    }

                    @Override
                    public void logAfterExecution(final StatementContext context) {
                        if (ManagementConfig.this.logSql) {
                            sqlLog.info("sql ae: " + context.getRenderedSql() + ", took " + context.getElapsedTime(ChronoUnit.MILLIS) + "ms");
                        }

                        final Object attr = context.getAttribute("observation");
                        if (attr instanceof Observation observation) {
                            observation.stop();
                        } else {
                            sqlLog.warn("No observation for " + this.getObservationName(context));
                        }
                    }

                    @Override
                    public void logException(final StatementContext context, final SQLException ex) {
                        if (ManagementConfig.this.logSql) {
                            sqlLog.info("sql e: " + context.getRenderedSql() + ", " + ex.getMessage());
                        }
                        final Object attr = context.getAttribute("observation");
                        if (attr instanceof Observation observation) {
                            observation.error(ex);
                            observation.stop();
                        } else {
                            sqlLog.warn("No observation for " + this.getObservationName(context));
                        }
                    }

                    private String getObservationName(final StatementContext context) {
                        final Method method = context.getExtensionMethod().getMethod();
                        return method.getDeclaringClass().getSimpleName() + "#" + method.getName();
                    }
                };
                jdbi.setSqlLogger(myLogger);
            }
        };
    }
}
