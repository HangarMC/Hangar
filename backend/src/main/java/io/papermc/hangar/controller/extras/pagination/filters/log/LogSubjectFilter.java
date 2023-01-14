package io.papermc.hangar.controller.extras.pagination.filters.log;

import io.papermc.hangar.controller.extras.pagination.Filter;
import io.papermc.hangar.controller.extras.pagination.filters.log.LogSubjectFilter.LogSubjectFilterInstance;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.jdbi.v3.core.statement.SqlStatement;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.NativeWebRequest;

@Component
public class LogSubjectFilter implements Filter<LogSubjectFilterInstance, String> {

    @Override
    public Set<String> getQueryParamNames() {
        return Set.of("subjectName");
    }

    @Override
    public String getDescription() {
        return "Filters by subject name, usually a user action where the subject name is the user the action is about, not the user that performed the action";
    }

    @Override
    public String getValue(final NativeWebRequest webRequest) {
        return webRequest.getParameter(this.getSingleQueryParam());
    }

    @Override
    public @NotNull LogSubjectFilterInstance create(final NativeWebRequest webRequest) {
        return new LogSubjectFilterInstance(this.getValue(webRequest));
    }

    static class LogSubjectFilterInstance implements Filter.FilterInstance {

        private final String subjectName;

        LogSubjectFilterInstance(final String subjectName) {
            this.subjectName = subjectName;
        }

        @Override
        public void createSql(final StringBuilder sb, final SqlStatement<?> q) {
            if (StringUtils.isNotBlank(this.subjectName)) {
                sb.append(" AND lower(la.s_name) = lower(:subjectName)");
                q.bind("subjectName", this.subjectName);
            }
        }
    }
}
