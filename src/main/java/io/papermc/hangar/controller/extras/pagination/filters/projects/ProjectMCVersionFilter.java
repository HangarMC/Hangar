package io.papermc.hangar.controller.extras.pagination.filters.projects;

import io.papermc.hangar.controller.extras.pagination.Filter;
import org.jdbi.v3.core.statement.SqlStatement;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.Arrays;
import java.util.Set;

@Component
public class ProjectMCVersionFilter implements Filter<ProjectMCVersionFilter.ProjectMCVersionFilterInstance> {

    private final ConversionService conversionService;

    @Autowired
    public ProjectMCVersionFilter(ConversionService conversionService) {
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

    @NotNull
    @Override
    public ProjectMCVersionFilterInstance create(NativeWebRequest webRequest) {
        return new ProjectMCVersionFilterInstance(conversionService.convert(webRequest.getParameterValues(getSingleQueryParam()), String[].class));
    }

    static class ProjectMCVersionFilterInstance implements FilterInstance {

        private final String[] versions;

        ProjectMCVersionFilterInstance(final String[] versions) {
            this.versions = versions;
        }

        @Override
        public void createSql(StringBuilder sb, SqlStatement<?> q) {
            sb.append(" AND v.version").append(" IN (");
            for (int i = 0; i < versions.length; i++) {
                sb.append(":__version__").append(i);
                if (i + 1 != versions.length) {
                    sb.append(",");
                }
                q.bind("__version__" + i, versions[i]);
            }
            sb.append(")");
        }

        @Override
        public String toString() {
            return "ProjectMCVersionFilterInstance{" +
                "versions=" + Arrays.toString(versions) +
                '}';
        }
    }
}
