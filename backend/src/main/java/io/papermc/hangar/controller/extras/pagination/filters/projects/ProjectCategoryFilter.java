package io.papermc.hangar.controller.extras.pagination.filters.projects;

import io.papermc.hangar.controller.extras.pagination.Filter;
import io.papermc.hangar.controller.extras.pagination.filters.projects.ProjectCategoryFilter.ProjectCategoryFilterInstance;
import io.papermc.hangar.model.common.projects.Category;
import java.util.Arrays;
import java.util.Set;
import org.jdbi.v3.core.statement.SqlStatement;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.NativeWebRequest;

@Component
public class ProjectCategoryFilter implements Filter<ProjectCategoryFilterInstance, String[]> {

    private final ConversionService conversionService;

    @Autowired
    public ProjectCategoryFilter(final ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    @Override
    public Set<String> getQueryParamNames() {
        return Set.of("category");
    }

    @Override
    public String getDescription() {
        return "A category to filter for";
    }

    @Override
    public String[] getValue(final NativeWebRequest webRequest) {
        return webRequest.getParameterValues(this.getSingleQueryParam());
    }

    @Override
    public @NotNull ProjectCategoryFilterInstance create(final NativeWebRequest webRequest) {
        return new ProjectCategoryFilterInstance(this.conversionService.convert(this.getValue(webRequest), Category[].class));
    }

    static class ProjectCategoryFilterInstance implements Filter.FilterInstance {

        private final Category[] categories;

        public ProjectCategoryFilterInstance(final Category[] categories) {
            this.categories = categories;
        }

        @Override
        public void createSql(final StringBuilder sb, final SqlStatement<?> q) {
            sb.append(" AND ").append("p.category").append(" IN (");
            for (int i = 0; i < this.categories.length; i++) {
                sb.append(":__category__").append(i);
                if (i + 1 != this.categories.length) {
                    sb.append(',');
                }
                q.bind("__category__" + i, this.categories[i]);
            }
            sb.append(')');
        }

        @Override
        public String toString() {
            return "ProjectCategoryFilterInstance{" +
                "categories=" + Arrays.toString(this.categories) +
                '}';
        }
    }
}
