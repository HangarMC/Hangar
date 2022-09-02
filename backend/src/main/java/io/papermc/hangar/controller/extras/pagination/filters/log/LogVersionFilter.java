package io.papermc.hangar.controller.extras.pagination.filters.log;

import io.papermc.hangar.controller.extras.pagination.Filter;
import io.papermc.hangar.controller.extras.pagination.filters.log.LogVersionFilter.LogVersionFilterInstance;
import io.papermc.hangar.model.common.Platform;
import org.jdbi.v3.core.statement.SqlStatement;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.Set;

@Component
public class LogVersionFilter implements Filter<LogVersionFilterInstance> {

    private final ConversionService conversionService;

    @Autowired
    public LogVersionFilter(ConversionService conversionService) {
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
    public boolean supports(NativeWebRequest webRequest) {
        return getQueryParamNames().stream().allMatch(webRequest.getParameterMap()::containsKey);
    }

    @NotNull
    @Override
    public LogVersionFilterInstance create(NativeWebRequest webRequest) {
        return new LogVersionFilterInstance(webRequest.getParameter("versionString"), conversionService.convert(webRequest.getParameter("platform"), Platform.class));
    }

    static class LogVersionFilterInstance implements FilterInstance {

        private final String versionString;
        private final Platform platform;

        LogVersionFilterInstance(String versionString, Platform platform) {
            this.versionString = versionString;
            this.platform = platform;
        }

        @Override
        public void createSql(StringBuilder sb, SqlStatement<?> q) {
            sb.append(" AND la.pv_version_string = :versionString");
            q.bind("versionString", this.versionString);
            sb.append(" AND :platform = ANY(la.pv_platforms)");
            q.bind("platform", this.platform);
        }
    }
}
