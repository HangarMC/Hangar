package io.papermc.hangar.controller.extras.pagination.filters.projects;

import io.papermc.hangar.controller.extras.pagination.Filter;
import java.util.Arrays;
import java.util.Set;
import org.jdbi.v3.core.statement.SqlStatement;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.NativeWebRequest;

@Component
public class ProjectMCVersionFilter implements Filter<ProjectMCVersionFilter.ProjectMCVersionFilterInstance, String[]> {

    private final ConversionService conversionService;

    @Autowired
    public ProjectMCVersionFilter(final ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    @Override
    public Set<String> getQueryParamNames() {
        return Set.of("version");
    }

    @Override
    public String getDescription() {
        return "A Minecraft version to filter for";
    }

    @Override
    public String[] getValue(final NativeWebRequest webRequest) {
        return webRequest.getParameterValues(this.getSingleQueryParam());
    }

    @Override
    public @NotNull ProjectMCVersionFilterInstance create(final NativeWebRequest webRequest) {
        return new ProjectMCVersionFilterInstance(this.conversionService.convert(this.getValue(webRequest), String[].class));
    }

    static class ProjectMCVersionFilterInstance implements Filter.FilterInstance {

        private final String[] versions;

        ProjectMCVersionFilterInstance(final String[] versions) {
            this.versions = versions;
        }

        @Override
        public void createSql(final StringBuilder sb, final SqlStatement<?> q) {
            sb.append(" AND v.version").append(" IN (");
            for (int i = 0; i < this.versions.length; i++) {
                sb.append(":__version__").append(i);
                if (i + 1 != this.versions.length) {
                    sb.append(',');
                }
                q.bind("__version__" + i, this.versions[i]);
            }
            sb.append(')');
        }

        @Override
        public String toString() {
            return "ProjectMCVersionFilterInstance{" +
                "versions=" + Arrays.toString(this.versions) +
                '}';
        }
    }
}
