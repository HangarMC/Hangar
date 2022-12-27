package io.papermc.hangar.controller.extras.pagination.filters.log;

import io.papermc.hangar.controller.extras.pagination.Filter;
import io.papermc.hangar.controller.extras.pagination.filters.log.LogProjectFilter.LogProjectFilterInstance;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.jdbi.v3.core.statement.SqlStatement;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.NativeWebRequest;

@Component
public class LogProjectFilter implements Filter<LogProjectFilterInstance> {

    @Override
    public Set<String> getQueryParamNames() {
        return Set.of("authorName", "projectSlug");
    }

    @Override
    public boolean supports(final NativeWebRequest webRequest) {
        return this.getQueryParamNames().stream().anyMatch(webRequest.getParameterMap()::containsKey);
    }

    @Override
    public String getDescription() {
        return "Filters logs by a project namespace";
    }

    @Override
    public @NotNull LogProjectFilterInstance create(final NativeWebRequest webRequest) {
        return new LogProjectFilterInstance(webRequest.getParameter("authorName"), webRequest.getParameter("projectSlug"));
    }

    static class LogProjectFilterInstance implements Filter.FilterInstance {

        private final String authorName;
        private final String projectSlug;

        LogProjectFilterInstance(final String authorName, final String projectSlug) {
            this.authorName = authorName;
            this.projectSlug = projectSlug;
        }

        @Override
        public void createSql(final StringBuilder sb, final SqlStatement<?> q) {
            if (StringUtils.isNotBlank(this.authorName)) {
                sb.append(" AND la.p_owner_name = :authorName");
                q.bind("authorName", this.authorName);
            }
            if (StringUtils.isNotBlank(this.projectSlug)) {
                sb.append(" AND la.p_slug = :projectSlug");
                q.bind("projectSlug", this.projectSlug);
            }
        }
    }
}
