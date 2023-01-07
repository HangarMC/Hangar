package io.papermc.hangar.controller.extras.pagination.filters.log;

import io.papermc.hangar.controller.extras.pagination.Filter;
import io.papermc.hangar.controller.extras.pagination.filters.log.LogActionFilter.LogActionFilterInstance;
import io.papermc.hangar.model.internal.logs.LogAction;
import java.util.Set;
import org.jdbi.v3.core.statement.SqlStatement;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.NativeWebRequest;

@Component
public class LogActionFilter implements Filter<LogActionFilterInstance, String> {

    @Override
    public Set<String> getQueryParamNames() {
        return Set.of("logAction");
    }

    @Override
    public String getDescription() {
        return "Filters by log action";
    }

    @Override
    public String getValue(final NativeWebRequest webRequest) {
        return webRequest.getParameter(this.getSingleQueryParam());
    }

    @Override
    public boolean supports(final NativeWebRequest webRequest) {
        return Filter.super.supports(webRequest) && LogAction.LOG_REGISTRY.containsKey(this.getValue(webRequest));
    }

    @Override
    public @NotNull LogActionFilterInstance create(final NativeWebRequest webRequest) {
        return new LogActionFilterInstance(LogAction.LOG_REGISTRY.get(this.getValue(webRequest)));
    }

    static class LogActionFilterInstance implements Filter.FilterInstance {

        private final LogAction<?> logAction;

        LogActionFilterInstance(final LogAction<?> logAction) {
            this.logAction = logAction;
        }

        @Override
        public void createSql(final StringBuilder sb, final SqlStatement<?> q) {
            sb.append(" AND la.action = :actionFilter::LOGGED_ACTION_TYPE");
            q.bind("actionFilter", this.logAction.getPgLoggedAction().getValue());
        }
    }
}
