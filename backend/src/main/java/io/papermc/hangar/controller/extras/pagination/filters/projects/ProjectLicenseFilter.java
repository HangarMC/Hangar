package io.papermc.hangar.controller.extras.pagination.filters.projects;

import org.jdbi.v3.core.statement.SqlStatement;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.Arrays;
import java.util.Set;

import io.papermc.hangar.controller.extras.pagination.Filter;

@Component
public class ProjectLicenseFilter implements Filter<ProjectLicenseFilter.ProjectLicenseFilterInstance> {

    private final ConversionService conversionService;

    @Autowired
    public ProjectLicenseFilter(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    @Override
    public Set<String> getQueryParamNames() {
        return Set.of("license");
    }

    @Override
    public String getDescription() {
        return "A license to filter for";
    }

    @NotNull
    @Override
    public ProjectLicenseFilterInstance create(NativeWebRequest webRequest) {
        return new ProjectLicenseFilterInstance(conversionService.convert(webRequest.getParameterValues(getSingleQueryParam()), String[].class));
    }

    static class ProjectLicenseFilterInstance implements FilterInstance {

        private final String[] licenses;

        public ProjectLicenseFilterInstance(String[] licenses) {
            this.licenses = licenses;
        }

        @Override
        public void createSql(StringBuilder sb, SqlStatement<?> q) {
            sb.append(" AND p.license_type").append(" IN (");
            for (int i = 0; i < licenses.length; i++) {
                sb.append(":__licenses__").append(i);
                if (i + 1 != licenses.length) {
                    sb.append(",");
                }
                q.bind("__licenses__" + i, licenses[i]);
            }
            sb.append(")");
        }

        @Override
        public String toString() {
            return "ProjectLicenseFilterInstance{" +
                   "licenses=" + Arrays.toString(licenses) +
                   '}';
        }
    }
}
