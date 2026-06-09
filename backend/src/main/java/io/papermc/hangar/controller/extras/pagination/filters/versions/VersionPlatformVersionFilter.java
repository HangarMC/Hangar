package io.papermc.hangar.controller.extras.pagination.filters.versions;

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
public class VersionPlatformVersionFilter implements Filter<VersionPlatformVersionFilter.VersionPlatformVersionFilterInstance, String[]> {

    private final ConversionService conversionService;

    @Autowired
    public VersionPlatformVersionFilter(final ConversionService conversionService) {
        this.conversionService = conversionService;
    }

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
        if (!webRequest.getParameterMap().containsKey("platform")) {
            throw new HangarApiException("Platform parameter is required for platform version filter");
        }

        return new VersionPlatformVersionFilterInstance(
            this.conversionService.convert(webRequest.getParameterValues("platform"), Platform[].class),
            this.conversionService.convert(this.getValue(webRequest), String[].class));
    }

    static class VersionPlatformVersionFilterInstance implements FilterInstance {

        private final Platform[] platforms;
        private final String[] versions;

        VersionPlatformVersionFilterInstance(final Platform[] platforms, final String[] versions) {
            this.platforms = platforms;
            this.versions = versions;
        }

        @Override
        public void createSql(final StringBuilder sb, final SqlStatement<?> q) {
            throw new UnsupportedOperationException("Not implemented yet");
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
                sb.append("platformDependencies.").append(platforms[i].name()).append(version);
                if (i + 1 != this.platforms.length) {
                    sb.append(" OR ");
                }
            }
            sb.append(")");
        }

        @Override
        public String toString() {
            return "VersionPlatformVersionFilterInstance{" +
                "versions=" + Arrays.toString(this.versions) +
                "platforms=" + Arrays.toString(this.platforms) +
                '}';
        }
    }
}
