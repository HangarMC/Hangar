package io.papermc.hangar.controller.extras.pagination.filters.log;

import io.papermc.hangar.controller.extras.pagination.Filter;
import io.papermc.hangar.controller.extras.pagination.filters.log.LogVersionFilter.LogVersionFilterInstance;
import io.papermc.hangar.model.common.Platform;
import java.util.Set;
import org.jdbi.v3.core.statement.SqlStatement;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.NativeWebRequest;

@Component
public class LogVersionFilter implements Filter<LogVersionFilterInstance, String[]> {

    private final ConversionService conversionService;

    @Autowired
    public LogVersionFilter(final ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    @Override
    public Set<String> getQueryParamNames() {
        return Set.of("versionString", "platform");
    }

    @Override
    public String getDescription() {
        return "Filters logs based on a version string and platform";
    }

    @Override
    public boolean supports(final NativeWebRequest webRequest) {
        return this.getQueryParamNames().stream().allMatch(webRequest.getParameterMap()::containsKey);
    }

    @Override
    public String[] getValue(final NativeWebRequest webRequest) {
        return new String[]{webRequest.getParameter("versionString"), webRequest.getParameter("platform")};
    }

    @Override
    public @NotNull LogVersionFilterInstance create(final NativeWebRequest webRequest) {
        final String[] value = this.getValue(webRequest);
        return new LogVersionFilterInstance(value[0], this.conversionService.convert(value[1], Platform.class));
    }

    static class LogVersionFilterInstance implements Filter.FilterInstance {

        private final String versionString;
        private final Platform platform;

        LogVersionFilterInstance(final String versionString, final Platform platform) {
            this.versionString = versionString;
            this.platform = platform;
        }

        @Override
        public void createSql(final StringBuilder sb, final SqlStatement<?> q) {
            sb.append(" AND la.pv_version_string = :versionString");
            q.bind("versionString", this.versionString);
            sb.append(" AND :platform = ANY(la.pv_platforms)");
            q.bind("platform", this.platform);
        }
    }
}
