package io.papermc.hangar.controller.extras.pagination.filters.versions;

import io.papermc.hangar.controller.extras.pagination.Filter;
import io.papermc.hangar.controller.extras.pagination.filters.versions.VersionPlatformFilter.VersionPlatformFilterInstance;
import io.papermc.hangar.model.common.Platform;
import org.jdbi.v3.core.statement.SqlStatement;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.NativeWebRequest;

@Component
public class VersionPlatformFilter implements Filter<VersionPlatformFilterInstance> {

    private final ConversionService conversionService;

    @Autowired
    public VersionPlatformFilter(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    @Override
    public String getQueryParamName() {
        return "platform";
    }

    @Override
    public String getDescription() {
        return "A platform name to filter for";
    }

    @NotNull
    @Override
    public VersionPlatformFilterInstance create(NativeWebRequest webRequest) {
        return new VersionPlatformFilterInstance(conversionService.convert(webRequest.getParameterValues(getQueryParamName()), Platform[].class));
    }

    static class VersionPlatformFilterInstance implements FilterInstance {

        private final Platform[] platforms;

        VersionPlatformFilterInstance(Platform[] platforms) {
            this.platforms = platforms;
        }

        @Override
        public void createSql(StringBuilder sb, SqlStatement<?> q) {
            sb.append(" AND (");
            for (int i = 0; i < platforms.length; i++) {
                sb.append(":__platform_").append(i).append(" = ANY(sq.platforms)");
                q.bind("__platform_" + i, platforms[i]);
                if (i + 1 != platforms.length) {
                    sb.append(" OR ");
                }
            }
            sb.append(")");
        }
    }
}
