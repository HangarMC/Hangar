package io.papermc.hangar.controller.extras.pagination.filters.projects;

import io.papermc.hangar.controller.extras.pagination.Filter;
import io.papermc.hangar.controller.extras.pagination.filters.projects.ProjectAuthorFilter.ProjectAuthorFilterInstance;
import org.jdbi.v3.core.statement.SqlStatement;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.Set;

@Component
public class ProjectAuthorFilter implements Filter<ProjectAuthorFilterInstance> {

    @Override
    public Set<String> getQueryParamNames() {
        return Set.of("owner");
    }

    @Override
    public String getDescription() {
        return "The author of the project";
    }

    @NotNull
    @Override
    public ProjectAuthorFilterInstance create(NativeWebRequest webRequest) {
        return new ProjectAuthorFilterInstance(webRequest.getParameter(getSingleQueryParam()));
    }

    static class ProjectAuthorFilterInstance implements FilterInstance {

        private final String ownerName;

        ProjectAuthorFilterInstance(String ownerName) {
            this.ownerName = ownerName;
        }

        @Override
        public void createSql(StringBuilder sb, SqlStatement<?> q) {
            sb.append(" AND ").append("p.owner_name").append(" = ").append(":ownerName");
            q.bind("ownerName", ownerName);
        }

        @Override
        public String toString() {
            return "ProjectAuthorFilterInstance{" +
                    "ownerName='" + ownerName + '\'' +
                    '}';
        }
    }
}
