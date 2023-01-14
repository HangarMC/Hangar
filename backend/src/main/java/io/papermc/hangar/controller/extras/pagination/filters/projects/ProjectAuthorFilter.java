package io.papermc.hangar.controller.extras.pagination.filters.projects;

import io.papermc.hangar.controller.extras.pagination.Filter;
import io.papermc.hangar.controller.extras.pagination.filters.projects.ProjectAuthorFilter.ProjectAuthorFilterInstance;
import java.util.Set;
import org.jdbi.v3.core.statement.SqlStatement;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.NativeWebRequest;

@Component
public class ProjectAuthorFilter implements Filter<ProjectAuthorFilterInstance, String> {

    @Override
    public Set<String> getQueryParamNames() {
        return Set.of("owner");
    }

    @Override
    public String getDescription() {
        return "The author of the project";
    }

    @Override
    public String getValue(final NativeWebRequest webRequest) {
        return webRequest.getParameter(this.getSingleQueryParam());
    }

    @Override
    public @NotNull ProjectAuthorFilterInstance create(final NativeWebRequest webRequest) {
        return new ProjectAuthorFilterInstance(this.getValue(webRequest));
    }

    static class ProjectAuthorFilterInstance implements Filter.FilterInstance {

        private final String ownerName;

        ProjectAuthorFilterInstance(final String ownerName) {
            this.ownerName = ownerName;
        }

        @Override
        public void createSql(final StringBuilder sb, final SqlStatement<?> q) {
            sb.append(" AND ").append("lower(p.owner_name)").append(" = ").append("lower(:ownerName)");
            q.bind("ownerName", this.ownerName);
        }

        @Override
        public String toString() {
            return "ProjectAuthorFilterInstance{" +
                "ownerName='" + this.ownerName + '\'' +
                '}';
        }
    }
}
