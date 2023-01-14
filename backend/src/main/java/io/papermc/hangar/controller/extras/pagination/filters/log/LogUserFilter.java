package io.papermc.hangar.controller.extras.pagination.filters.log;

import io.papermc.hangar.controller.extras.pagination.Filter;
import io.papermc.hangar.controller.extras.pagination.filters.log.LogUserFilter.LogUserFilterInstance;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.jdbi.v3.core.statement.SqlStatement;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.NativeWebRequest;

@Component
public class LogUserFilter implements Filter<LogUserFilterInstance, String> {

    @Override
    public Set<String> getQueryParamNames() {
        return Set.of("user");
    }

    @Override
    public String getDescription() {
        return "The user whose action created the log entry";
    }

    @Override
    public String getValue(final NativeWebRequest webRequest) {
        return webRequest.getParameter(this.getSingleQueryParam());
    }

    @Override
    public @NotNull LogUserFilterInstance create(final NativeWebRequest webRequest) {
        return new LogUserFilterInstance(this.getValue(webRequest));
    }

    static class LogUserFilterInstance implements Filter.FilterInstance {

        private final String userName;

        LogUserFilterInstance(final String userName) {
            this.userName = userName;
        }

        @Override
        public void createSql(final StringBuilder sb, final SqlStatement<?> q) {
            if (StringUtils.isNotBlank(this.userName)) {
                sb.append(" AND lower(la.user_name) = lower(:userName)");
                q.bind("userName", this.userName);
            }
        }

        @Override
        public String toString() {
            return "LogUserFilterInstance{" +
                "userName='" + this.userName + '\'' +
                '}';
        }
    }
}
