package io.papermc.hangar.controller.extras.pagination;

import io.papermc.hangar.model.common.projects.Category;
import org.jdbi.v3.core.statement.SqlStatement;
import org.springframework.web.context.request.NativeWebRequest;

public class ProjectCategoryFilter implements Filter {

    private static final String QUERY_PARAM = "projectCategory";

    private final Category category;

    public ProjectCategoryFilter(NativeWebRequest webRequest) {
        this.category = Category.fromValue(webRequest.getParameterMap().get(QUERY_PARAM)[0]);
    }

    @Override
    public void createSql(StringBuilder sb, SqlStatement<?> q) {
        sb.append(" AND ").append("p.category").append(" = :").append("category");
        q.bind("category", category);
    }

    public static boolean supports(NativeWebRequest webRequest) {
        return webRequest.getParameterMap().containsKey(QUERY_PARAM);
    }
}
