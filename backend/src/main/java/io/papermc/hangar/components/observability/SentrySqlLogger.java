package io.papermc.hangar.components.observability;

import io.sentry.ISpan;
import io.sentry.Sentry;
import io.sentry.SpanStatus;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.time.temporal.ChronoUnit;
import org.jdbi.v3.core.statement.SqlLogger;
import org.jdbi.v3.core.statement.StatementContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SentrySqlLogger implements SqlLogger {

    private static final Logger sqlLog = LoggerFactory.getLogger("sql");
    private final boolean logSql = false; // for debugging sql statements

    @Override
    public void logBeforeExecution(final StatementContext context) {
        if (this.logSql) {
            sqlLog.info("sql be: " + context.getRenderedSql());
        }

        ISpan parentSpan = Sentry.getSpan();
        if (parentSpan == null) {
            sqlLog.debug("No parent span for {}", this.getObservationName(context));
            parentSpan = Sentry.startTransaction("unknown", "task");
        }

        ISpan childSpan = parentSpan.startChild("db.query", context.getRenderedSql());
        childSpan.setData("db.system", "postgresql");
        childSpan.setData("db.name", "hangar");
        childSpan.setData("code.namespace", "Hangar");
        childSpan.setData("code.lineno", "1");
        if (context.getExtensionMethod() != null) {
            childSpan.setData("code.filepath", context.getExtensionMethod().getMethod().getDeclaringClass().getSimpleName());
            childSpan.setData("code.function", context.getExtensionMethod().getMethod().getName());
        }
        childSpan.setData("db.binding", context.getBinding().toString());
        context.define("span", childSpan);
    }

    @Override
    public void logAfterExecution(final StatementContext context) {
        if (this.logSql) {
            sqlLog.info("sql ae: " + context.getRenderedSql() + ", took " + context.getElapsedTime(ChronoUnit.MILLIS) + "ms");
        }

        final Object attr = context.getAttribute("span");
        if (attr instanceof ISpan span) {
            span.setStatus(SpanStatus.OK);
            span.finish();
        } else {
            sqlLog.warn("No span for {}", this.getObservationName(context));
        }
    }

    @Override
    public void logException(final StatementContext context, final SQLException ex) {
        if (this.logSql) {
            sqlLog.info("sql e: " + context.getRenderedSql() + ", " + ex.getMessage());
        }
        final Object attr = context.getAttribute("span");
        if (attr instanceof ISpan span) {
            span.setThrowable(ex);
            span.setStatus(SpanStatus.INTERNAL_ERROR);
            span.finish();
        } else {
            sqlLog.warn("No span for {}", this.getObservationName(context));
        }
    }

    private String getObservationName(final StatementContext context) {
        if (context.getExtensionMethod() == null) {
            return "unknown";
        }
        final Method method = context.getExtensionMethod().getMethod();
        return method.getDeclaringClass().getSimpleName() + "#" + method.getName();
    }
}
