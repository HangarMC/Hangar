package io.papermc.hangar.controller.extras.pagination.filters.versions;

import io.papermc.hangar.controller.extras.pagination.Filter;
import io.papermc.hangar.controller.extras.pagination.filters.versions.VersionPlatformFilter.VersionPlatformFilterInstance;
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
public class VersionPlatformFilter implements Filter<VersionPlatformFilterInstance, String[]> {

    private final ConversionService conversionService;

    @Autowired
    public VersionPlatformFilter(final ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    @Override
    public Set<String> getQueryParamNames() {
        return Set.of("platform");
    }

    @Override
    public String getDescription() {
        return "A platform name to filter for";
    }

    @Override
    public String[] getValue(final NativeWebRequest webRequest) {
        return webRequest.getParameterValues(this.getSingleQueryParam());
    }

    @Override
    public @NotNull VersionPlatformFilterInstance create(final NativeWebRequest webRequest) {
        return new VersionPlatformFilterInstance(this.conversionService.convert(this.getValue(webRequest), Platform[].class));
    }

    public static class VersionPlatformFilterInstance implements Filter.FilterInstance {

        private final Platform[] platforms;

        public VersionPlatformFilterInstance(final Platform[] platforms) {
            this.platforms = platforms;
        }

        @Override
        public void createSql(final StringBuilder sb, final SqlStatement<?> q) {
            sb.append(" AND (");
            for (int i = 0; i < this.platforms.length; i++) {
                sb.append(":__platform_").append(i).append(" = ANY(sq.platforms)");
                q.bind("__platform_" + i, this.platforms[i]);
                if (i + 1 != this.platforms.length) {
                    sb.append(" OR ");
                }
            }
            sb.append(")");
        }

        @Override
        public String toString() {
            return "VersionPlatformFilterInstance{" +
                "platforms=" + Arrays.toString(this.platforms) +
                '}';
        }
    }
}
