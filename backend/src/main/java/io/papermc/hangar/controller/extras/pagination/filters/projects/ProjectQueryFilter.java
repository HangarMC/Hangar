package io.papermc.hangar.controller.extras.pagination.filters.projects;

import io.papermc.hangar.controller.extras.pagination.Filter;
import io.papermc.hangar.controller.extras.pagination.filters.projects.ProjectQueryFilter.ProjectQueryFilterInstance;
import java.util.Set;
import org.jdbi.v3.core.statement.SqlStatement;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.NativeWebRequest;

@Component
public class ProjectQueryFilter implements Filter<ProjectQueryFilterInstance> {

    @Override
    public Set<String> getQueryParamNames() {
        return Set.of("q");
    }

    @Override
    public String getDescription() {
        return "The query to use when searching";
    }

    @Override
    public @NotNull ProjectQueryFilterInstance create(final NativeWebRequest webRequest) {
        return new ProjectQueryFilterInstance(webRequest.getParameter(this.getSingleQueryParam()));
    }

    static class ProjectQueryFilterInstance implements Filter.FilterInstance {

        private final String query;

        ProjectQueryFilterInstance(final String query) {
            this.query = query;
        }

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
