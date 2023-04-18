package io.papermc.hangar.controller.extras.pagination.filters.projects;

import io.papermc.hangar.controller.extras.pagination.Filter;
import io.papermc.hangar.model.api.project.settings.Tag;
import java.util.Arrays;
import java.util.Set;
import org.jdbi.v3.core.statement.SqlStatement;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.NativeWebRequest;

@Component
public class ProjectTagFilter implements Filter<ProjectTagFilter.ProjectTagFilterInstance, String[]> {

    private final ConversionService conversionService;

    @Autowired
    public ProjectTagFilter(final ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    @Override
    public Set<String> getQueryParamNames() {
        return Set.of("tag");
    }

    @Override
    public String getDescription() {
        return "A tag to filter for";
    }

    @Override
    public String[] getValue(final NativeWebRequest webRequest) {
        return webRequest.getParameterValues(this.getSingleQueryParam());
    }

    @Override
    public @NotNull ProjectTagFilter.ProjectTagFilterInstance create(final NativeWebRequest webRequest) {
        return new ProjectTagFilter.ProjectTagFilterInstance(this.conversionService.convert(this.getValue(webRequest), Tag[].class));
    }

    static class ProjectTagFilterInstance implements Filter.FilterInstance {

        private final Tag[] tags;

        public ProjectTagFilterInstance(final Tag[] tags) {
            this.tags = tags;
        }

        @Override
        public void createSql(final StringBuilder sb, final SqlStatement<?> q) {
            sb.append(" AND p.tags").append(" @> '{");
            boolean first = true;
            for (final Tag tag : this.tags) {
                if (first) {
                    first = false;
                } else {
                    sb.append(',');
                }
                sb.append('"').append(tag).append('"');
            }
            sb.append("}'");
        }

        @Override
        public String toString() {
            return "ProjectTagFilterInstance{" +
                "tag=" + Arrays.toString(this.tags) +
                '}';
        }
    }
}
