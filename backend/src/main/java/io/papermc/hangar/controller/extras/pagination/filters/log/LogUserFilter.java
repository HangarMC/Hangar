package io.papermc.hangar.controller.extras.pagination.filters.log;

import io.papermc.hangar.controller.extras.pagination.Filter;
import io.papermc.hangar.controller.extras.pagination.filters.log.LogUserFilter.LogUserFilterInstance;
import org.jdbi.v3.core.statement.SqlStatement;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.Set;

@Component
public class LogUserFilter implements Filter<LogUserFilterInstance> {

    @Override
    public Set<String> getQueryParamNames() {
        return Set.of("user");
    }

    @Override
    public String getDescription() {
        return "The user whose action created the log entry";
    }

    @Override
    public @NotNull LogUserFilterInstance create(NativeWebRequest webRequest) {
        return new LogUserFilterInstance(webRequest.getParameter(getSingleQueryParam()));
    }

    static class LogUserFilterInstance implements FilterInstance {

        private final String userName;

        LogUserFilterInstance(String userName) {
            this.userName = userName;
        }

        @Override
        public void createSql(StringBuilder sb, SqlStatement<?> q) {
            sb.append(" AND la.user_name = :userName");
            q.bind("userName", userName);
        }

        @Override
        public String toString() {
            return "LogUserFilterInstance{" +
                    "userName='" + userName + '\'' +
                    '}';
        }
    }
}
