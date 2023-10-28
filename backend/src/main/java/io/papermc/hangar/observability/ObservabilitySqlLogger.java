package io.papermc.hangar.observability;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import org.jdbi.v3.core.statement.SqlLogger;
import org.jdbi.v3.core.statement.StatementContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.time.temporal.ChronoUnit;

import static io.micrometer.observation.Observation.createNotStarted;

public class ObservabilitySqlLogger implements SqlLogger {

    private static final Logger sqlLog = LoggerFactory.getLogger("sql");
    private final boolean logSql = false; // for debugging sql statements

    private final ObservationRegistry observationRegistry;

    public ObservabilitySqlLogger(final ObservationRegistry observationRegistry) {
        this.observationRegistry = observationRegistry;
    }

    @Override
    public void logBeforeExecution(final StatementContext context) {
        if (this.logSql) {
            sqlLog.info("sql be: " + context.getRenderedSql());
        }

        final Observation observation = createNotStarted(this.getObservationName(context), this.observationRegistry);
        observation.start();
        observation.lowCardinalityKeyValue("hangar.sql.binding", context.getBinding().toString());
        observation.highCardinalityKeyValue("hangar.sql.rendered", context.getRenderedSql());
        observation.highCardinalityKeyValue("hangar.type", "SQL");
        context.define("observation", observation);
    }

    @Override
    public void logAfterExecution(final StatementContext context) {
        if (this.logSql) {
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
        if (this.logSql) {
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
        if (context.getExtensionMethod() == null) {
            return "unknown";
        }
        final Method method = context.getExtensionMethod().getMethod();
        return method.getDeclaringClass().getSimpleName().replace("DAO", "dao") + "#" + method.getName();
    }
}
