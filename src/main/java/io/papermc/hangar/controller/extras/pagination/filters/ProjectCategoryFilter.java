package io.papermc.hangar.controller.extras.pagination.filters;

import io.papermc.hangar.controller.extras.pagination.Filter;
import io.papermc.hangar.controller.extras.pagination.filters.ProjectCategoryFilter.ProjectCategoryFilterInstance;
import io.papermc.hangar.model.common.projects.Category;
import org.jdbi.v3.core.statement.SqlStatement;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.Arrays;

@Component
public class ProjectCategoryFilter implements Filter<ProjectCategoryFilterInstance> {

    private final ConversionService conversionService;

    @Autowired
    public ProjectCategoryFilter(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    @NotNull
    @Override
    public ProjectCategoryFilterInstance create(NativeWebRequest webRequest) {
        return new ProjectCategoryFilterInstance(conversionService.convert(webRequest.getParameterValues(getQueryParamName()), Category[].class));
    }

    @Override
    public String getQueryParamName() {
        return "category";
    }

    static class ProjectCategoryFilterInstance implements FilterInstance {

        private final Category[] categories;

        public ProjectCategoryFilterInstance(Category[] categories) {
            this.categories = categories;
            System.out.println(Arrays.toString(categories));
        }

        @Override
        public void createSql(StringBuilder sb, SqlStatement<?> q) {
            // TODO this is hacky af but idk a better way. Can't use defined arguments inside another defined argument apparently
            sb.append(" AND ").append("p.category").append(" IN (");
            for (int i = 0; i < categories.length; i++) {
                sb.append(":__category__").append(i);
                if (i + 1 != categories.length) {
                    sb.append(",");
                }
                q.bind("__category__" + i, categories[i]);
            }
            sb.append(")");
        }
    }
}
