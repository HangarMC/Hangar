package io.papermc.hangar.controller.extras.pagination.filters.projects;

import io.papermc.hangar.controller.extras.pagination.Filter;
import io.papermc.hangar.model.common.Platform;
import java.util.Arrays;
import java.util.Set;
import org.jdbi.v3.core.statement.SqlStatement;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.NativeWebRequest;

@Component
public class ProjectPlatformFilter implements Filter<ProjectPlatformFilter.ProjectPlatformFilterInstance, String[]> {

    private final ConversionService conversionService;

    @Autowired
    public ProjectPlatformFilter(final ConversionService conversionService) {
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

    @Override
    public String[] getValue(final NativeWebRequest webRequest) {
        return webRequest.getParameterValues(this.getSingleQueryParam());
    }

    @Override
    public @NotNull ProjectPlatformFilterInstance create(final NativeWebRequest webRequest) {
        return new ProjectPlatformFilterInstance(this.conversionService.convert(this.getValue(webRequest), Platform[].class));
    }

    static class ProjectPlatformFilterInstance implements Filter.FilterInstance {

        private final Platform[] platforms;

        public ProjectPlatformFilterInstance(final Platform[] platforms) {
            this.platforms = platforms;
        }

        @Override
        public void createSql(final StringBuilder sb, final SqlStatement<?> q) {
            sb.append(" AND v.platform").append(" IN (");
            for (int i = 0; i < this.platforms.length; i++) {
                sb.append(":__platform__").append(i);
                if (i + 1 != this.platforms.length) {
                    sb.append(',');
                }
                q.bind("__platform__" + i, this.platforms[i]);
            }
            sb.append(')');
        }

        @Override
        public String toString() {
            return "ProjectPlatformFilterInstance{" +
                "platforms=" + Arrays.toString(this.platforms) +
                '}';
        }
    }
}
