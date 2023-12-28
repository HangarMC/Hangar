package io.papermc.hangar.controller.extras.pagination.filters.projects;

import io.papermc.hangar.controller.extras.pagination.Filter;
import io.papermc.hangar.controller.extras.pagination.filters.projects.ProjectQueryFilter.ProjectQueryFilterInstance;
import io.papermc.hangar.exceptions.HangarApiException;
import java.util.Map;
import java.util.Set;
import org.jdbi.v3.core.statement.SqlStatement;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.server.ResponseStatusException;

@Component
public class ProjectQueryFilter implements Filter<ProjectQueryFilterInstance, String> {

    @Override
    public Set<String> getQueryParamNames() {
        return Set.of("query", "q");
    }

    @Override
    public Map<String, String> getDeprecatedQueryParamNames() {
        return Map.of("q", "Use 'query' instead");
    }

    @Override
    public String getDescription() {
        return "The query to use when searching";
    }

    @Override
    public String getValue(final NativeWebRequest webRequest) {
        if (webRequest.getParameterMap().containsKey("query")) {
            if (webRequest.getParameterMap().containsKey("q")) {
                throw new HangarApiException(HttpStatus.BAD_REQUEST, "Cannot have both 'query' and 'q' parameters");
            }
            return webRequest.getParameter("query");
        }
        return webRequest.getParameter("q");
    }

    @Override
    public @NotNull ProjectQueryFilterInstance create(final NativeWebRequest webRequest) {
        return new ProjectQueryFilterInstance(this.getValue(webRequest));
    }

    public record ProjectQueryFilterInstance(String query) implements Filter.FilterInstance {

        @Override
            public void createSql(final StringBuilder sb, final SqlStatement<?> q) {
                sb.append(" AND (hp.search_words @@ websearch_to_tsquery");
                if (!this.query.endsWith(" ")) {
                    sb.append("_postfix");
                }
                sb.append("('english', :query)").append(")");
                q.bind("query", this.query.trim());
            }

            @Override
            public String toString() {
                return "ProjectQueryFilterInstance{" +
                        "query='" + this.query + '\'' +
                        '}';
            }
        }
}
