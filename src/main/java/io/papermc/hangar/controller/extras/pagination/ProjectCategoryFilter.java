package io.papermc.hangar.controller.extras.pagination;

import io.papermc.hangar.controller.extras.pagination.ProjectCategoryFilter.ProjectCategoryFilterInstance;
import io.papermc.hangar.model.common.projects.Category;
import org.jdbi.v3.core.statement.SqlStatement;
import org.springframework.web.context.request.NativeWebRequest;

public class ProjectCategoryFilter implements Filter<ProjectCategoryFilterInstance> {

    private static final String QUERY_PARAM = "projectCategory";

    @Override
    public boolean supports(NativeWebRequest webRequest) {
        return webRequest.getParameterMap().containsKey(QUERY_PARAM);
    }

    @Override
    public ProjectCategoryFilterInstance create(NativeWebRequest webRequest) {
        return new ProjectCategoryFilterInstance(webRequest);
    }

    static class ProjectCategoryFilterInstance implements FilterInstance {

        private final Category category;

        public ProjectCategoryFilterInstance(NativeWebRequest webRequest) {
            this.category = Category.fromValue(webRequest.getParameterMap().get(QUERY_PARAM)[0]);
        }

        @Override
        public void createSql(StringBuilder sb, SqlStatement<?> q) {
            sb.append(" AND ").append("p.category").append(" = :").append("category");
            q.bind("category", category);
        }
    }
}
