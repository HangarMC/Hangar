package io.papermc.hangar.controller.extras.pagination.filters.projects;

import io.papermc.hangar.controller.extras.pagination.Filter;
import io.papermc.hangar.exceptions.HangarApiException;
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
public class ProjectPlatformVersionFilter implements Filter<ProjectPlatformVersionFilter.ProjectPlatformVersionFilterInstance, String[]> {

    private final ConversionService conversionService;

    @Autowired
    public ProjectPlatformVersionFilter(final ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    @Override
    public Set<String> getQueryParamNames() {
        return Set.of("version");
    }

    @Override
    public String getDescription() {
        return "A platform version to filter for";
    }

    @Override
    public String[] getValue(final NativeWebRequest webRequest) {
        return webRequest.getParameterValues(this.getSingleQueryParam());
    }

    @Override
    public @NotNull ProjectPlatformVersionFilterInstance create(final NativeWebRequest webRequest) {
        if (!webRequest.getParameterMap().containsKey("platform")) {
            throw new HangarApiException("Platform parameter is required for platform version filter");
        }

        return new ProjectPlatformVersionFilterInstance(
            this.conversionService.convert(webRequest.getParameterValues("platform"), Platform[].class),
            this.conversionService.convert(this.getValue(webRequest), String[].class));
    }

    static class ProjectPlatformVersionFilterInstance implements Filter.FilterInstance {

        private final Platform[] platforms;
        private final String[] versions;

        ProjectPlatformVersionFilterInstance(final Platform[] platforms, final String[] versions) {
            this.platforms = platforms;
            this.versions = versions;
        }

        @Override
        public void createSql(final StringBuilder sb, final SqlStatement<?> q) {
            sb.append(" ");
            sb.append("""
                AND EXISTS (
                    SELECT 1
                    FROM jsonb_array_elements(hp.supported_platforms) AS sp,
                          jsonb_array_elements_text(sp->'versions') AS version
                    WHERE version IN (
                """);
            for (int i = 0; i < this.versions.length; i++) {
                sb.append(":__version__").append(i);
                if (i + 1 != this.versions.length) {
                    sb.append(',');
                }
                q.bind("__version__" + i, this.versions[i]);
            }
            sb.append("))");
        }

        @Override
        public void createMeili(final StringBuilder sb) {
            StringBuilder versionSb = new StringBuilder(" IN [");
            for (int i = 0; i < this.versions.length; i++) {
                versionSb.append(versions[i]);
                if (i + 1 != this.versions.length) {
                    versionSb.append(",");
                }
            }
            versionSb.append("]");
            String version = versionSb.toString();

            sb.append("(");
            for (int i = 0; i < this.platforms.length; i++) {
                sb.append("supportedPlatforms.").append(platforms[i].name()).append(version);
                if (i + 1 != this.platforms.length) {
                    sb.append(" OR ");
                }
            }
            sb.append(")");
        }

        @Override
        public String toString() {
            return "ProjectPlatformVersionFilter{" +
                "versions=" + Arrays.toString(this.versions) +
                '}';
        }
    }
}
