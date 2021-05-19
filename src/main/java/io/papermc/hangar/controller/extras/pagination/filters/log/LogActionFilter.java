package io.papermc.hangar.controller.extras.pagination.filters.log;

import io.papermc.hangar.controller.extras.pagination.Filter;
import io.papermc.hangar.controller.extras.pagination.filters.log.LogActionFilter.LogActionFilterInstance;
import io.papermc.hangar.model.internal.logs.LogAction;
import org.jdbi.v3.core.statement.SqlStatement;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.Set;

@Component
public class LogActionFilter implements Filter<LogActionFilterInstance> {

    @Override
    public Set<String> getQueryParamNames() {
        return Set.of("logAction");
    }

    @Override
    public String getDescription() {
        return "Filters by log action";
    }

    @Override
    public boolean supports(NativeWebRequest webRequest) {
        return Filter.super.supports(webRequest) && LogAction.LOG_REGISTRY.containsKey(webRequest.getParameter(getSingleQueryParam()));
    }

    @NotNull
    @Override
    public LogActionFilterInstance create(NativeWebRequest webRequest) {
        return new LogActionFilterInstance(LogAction.LOG_REGISTRY.get(webRequest.getParameter(getSingleQueryParam())));
    }

    static class LogActionFilterInstance implements FilterInstance {

        private final LogAction<?> logAction;

        LogActionFilterInstance(LogAction<?> logAction) {
            this.logAction = logAction;
        }

        @Override
        public void createSql(StringBuilder sb, SqlStatement<?> q) {
            sb.append(" AND la.action = :actionFilter::LOGGED_ACTION_TYPE");
            q.bind("actionFilter", logAction.getPgLoggedAction().getValue());
        }
    }
}
