package io.papermc.hangar.controller.extras.pagination.filters.versions;

import io.papermc.hangar.controller.extras.pagination.Filter;
import io.papermc.hangar.controller.extras.pagination.filters.versions.VersionPlatformVersionFilter.VersionPlatformVersionFilterInstance;
import java.util.Arrays;
import java.util.Set;
import org.jdbi.v3.core.statement.SqlStatement;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.NativeWebRequest;

@Component
public class VersionPlatformVersionFilter implements Filter<VersionPlatformVersionFilterInstance, String[]> {

    @Override
    public Set<String> getQueryParamNames() {
        return Set.of("platformVersion");
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
    public @NotNull VersionPlatformVersionFilterInstance create(final NativeWebRequest webRequest) {
        return new VersionPlatformVersionFilterInstance(this.getValue(webRequest));
    }

    static class VersionPlatformVersionFilterInstance implements Filter.FilterInstance {

        private final String[] platformVersions;

        public VersionPlatformVersionFilterInstance(final String[] platformVersions) {
            this.platformVersions = platformVersions;
        }

        @Override
        public void createSql(final StringBuilder sb, final SqlStatement<?> q) {
            sb.append(" AND (");
            for (int i = 0; i < this.platformVersions.length; i++) {
                sb.append(":__platform_version_").append(i).append(" = ANY(sq.versions)");
                q.bind("__platform_version_" + i, this.platformVersions[i]);
                if (i + 1 != this.platformVersions.length) {
                    sb.append(" OR ");
                }
            }
            sb.append(')');
        }

        @Override
        public String toString() {
            return "VersionPlatformVersionFilterInstance{" +
                "platformVersions=" + Arrays.toString(this.platformVersions) +
                '}';
        }
    }
}
