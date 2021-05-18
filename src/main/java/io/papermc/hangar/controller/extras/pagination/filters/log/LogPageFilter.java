package io.papermc.hangar.controller.extras.pagination.filters.log;

import io.papermc.hangar.controller.extras.pagination.Filter;
import io.papermc.hangar.controller.extras.pagination.filters.log.LogPageFilter.LogPageFilterInstance;
import org.jdbi.v3.core.statement.SqlStatement;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.Set;

@Component
public class LogPageFilter implements Filter<LogPageFilterInstance> {

    @Override
    public Set<String> getQueryParamNames() {
        return Set.of("pageId");
    }

    @Override
    public String getDescription() {
        return "Filters based on a project page";
    }

    @Override
    public boolean supports(NativeWebRequest webRequest) {
        Long.parseLong(webRequest.getParameter(getSingleQueryParam()));
        return true;
    }

    @NotNull
    @Override
    public LogPageFilterInstance create(NativeWebRequest webRequest) {
        return new LogPageFilterInstance(Long.parseLong(webRequest.getParameter(getSingleQueryParam())));
    }

    static class LogPageFilterInstance implements FilterInstance {

        private final long pageId;

        LogPageFilterInstance(long pageId) {
            this.pageId = pageId;
        }

        @Override
        public void createSql(StringBuilder sb, SqlStatement<?> q) {
            sb.append(" AND la.pp_id = :pageId");
            q.bind("pageId", this.pageId);
        }
    }
}
