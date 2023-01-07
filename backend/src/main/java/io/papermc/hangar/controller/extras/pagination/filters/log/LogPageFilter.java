package io.papermc.hangar.controller.extras.pagination.filters.log;

import io.papermc.hangar.controller.extras.pagination.Filter;
import io.papermc.hangar.controller.extras.pagination.filters.log.LogPageFilter.LogPageFilterInstance;
import java.util.Set;
import org.apache.commons.lang3.math.NumberUtils;
import org.jdbi.v3.core.statement.SqlStatement;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.NativeWebRequest;

@Component
public class LogPageFilter implements Filter<LogPageFilterInstance, String> {

    @Override
    public Set<String> getQueryParamNames() {
        return Set.of("pageId");
    }

    @Override
    public String getDescription() {
        return "Filters based on a project page";
    }

    @Override
    public boolean supports(final NativeWebRequest webRequest) {
        return Filter.super.supports(webRequest) && NumberUtils.isDigits(this.getValue(webRequest));
    }

    @Override
    public String getValue(final NativeWebRequest webRequest) {
        return webRequest.getParameter(this.getSingleQueryParam());
    }

    @Override
    public @NotNull LogPageFilterInstance create(final NativeWebRequest webRequest) {
        return new LogPageFilterInstance(Long.parseLong(this.getValue(webRequest)));
    }

    static class LogPageFilterInstance implements Filter.FilterInstance {

        private final long pageId;

        LogPageFilterInstance(final long pageId) {
            this.pageId = pageId;
        }

        @Override
        public void createSql(final StringBuilder sb, final SqlStatement<?> q) {
            sb.append(" AND la.pp_id = :pageId");
            q.bind("pageId", this.pageId);
        }
    }
}
