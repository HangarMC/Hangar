package io.papermc.hangar.controller.extras.pagination.filters.projects;

import io.papermc.hangar.controller.extras.pagination.Filter;
import io.papermc.hangar.model.common.Platform;
import org.jdbi.v3.core.statement.SqlStatement;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.Arrays;
import java.util.Set;

@Component
public class ProjectPlatformFilter implements Filter<ProjectPlatformFilter.ProjectPlatformFilterInstance> {

    private final ConversionService conversionService;

    @Autowired
    public ProjectPlatformFilter(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    @Override
    public Set<String> getQueryParamNames() {
        return Set.of("platform");
    }

    @Override
    public String getDescription() {
        return "A platform to filter for";
    }

    @NotNull
    @Override
    public ProjectPlatformFilterInstance create(NativeWebRequest webRequest) {
        return new ProjectPlatformFilterInstance(conversionService.convert(webRequest.getParameterValues(getSingleQueryParam()), Platform[].class));
    }

    static class ProjectPlatformFilterInstance implements FilterInstance {

        private final Platform[] platforms;

        public ProjectPlatformFilterInstance(Platform[] platforms) {
            this.platforms = platforms;
        }

        @Override
        public void createSql(StringBuilder sb, SqlStatement<?> q) {
            sb.append(" AND v.platform").append(" IN (");
            for (int i = 0; i < platforms.length; i++) {
                sb.append(":__platform__").append(i);
                if (i + 1 != platforms.length) {
                    sb.append(",");
                }
                q.bind("__platform__" + i, platforms[i]);
            }
            sb.append(")");
        }

        @Override
        public String toString() {
            return "ProjectCategoryFilterInstance{" +
                    "platforms=" + Arrays.toString(platforms) +
                    '}';
        }
    }
}
