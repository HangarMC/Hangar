package io.papermc.hangar.controller.extras.pagination.filters.projects;

import io.papermc.hangar.controller.extras.pagination.Filter;
import io.papermc.hangar.controller.extras.pagination.filters.projects.ProjectQueryFilter.ProjectQueryFilterInstance;
import org.jdbi.v3.core.statement.SqlStatement;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.Set;

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

    @NotNull
    @Override
    public ProjectQueryFilterInstance create(NativeWebRequest webRequest) {
        return new ProjectQueryFilterInstance(webRequest.getParameter(getSingleQueryParam()));
    }

    static class ProjectQueryFilterInstance implements FilterInstance {

        private final String query;

        ProjectQueryFilterInstance(String query) {
            this.query = query;
        }

        @Override
        public void createSql(StringBuilder sb, SqlStatement<?> q) {
            /*sb.append(" AND (hp.search_words @@ websearch_to_tsquery");
            if (!query.endsWith(" ")) {
                sb.append("_postfix");
            }
            sb.append("('english', :query)").append(")");
            q.bind("query", query.trim());*/
            // TODO broken. Full-text search is not implemented in cockroachdb yet. See: https://go.crdb.dev/issue-v/7821/v21.2. Until it's done, we'll just do simple search
            sb.append(" AND (hp.name ILIKE '%" + query + "%')");
        }

        @Override
        public String toString() {
            return "ProjectQueryFilterInstance{" +
                    "query='" + query + '\'' +
                    '}';
        }
    }
}
