package io.papermc.hangar.controller.extras.pagination.filters.log;

import io.papermc.hangar.controller.extras.pagination.Filter;
import io.papermc.hangar.controller.extras.pagination.filters.log.LogSubjectFilter.LogSubjectFilterInstance;
import org.jdbi.v3.core.statement.SqlStatement;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.Set;

@Component
public class LogSubjectFilter implements Filter<LogSubjectFilterInstance> {

    @Override
    public Set<String> getQueryParamNames() {
        return Set.of("subjectName");
    }

    @Override
    public String getDescription() {
        return "Filters by subject name, usually a user action where the subject name is the user the action is about, not the user that performed the action";
    }

    @NotNull
    @Override
    public LogSubjectFilterInstance create(NativeWebRequest webRequest) {
        return new LogSubjectFilterInstance(webRequest.getParameter(getSingleQueryParam()));
    }

    static class LogSubjectFilterInstance implements FilterInstance {

        private final String subjectName;

        LogSubjectFilterInstance(String subjectName) {
            this.subjectName = subjectName;
        }

        @Override
        public void createSql(StringBuilder sb, SqlStatement<?> q) {
            sb.append(" AND la.s_name = :subjectName");
            q.bind("subjectName", this.subjectName);
        }
    }
}
