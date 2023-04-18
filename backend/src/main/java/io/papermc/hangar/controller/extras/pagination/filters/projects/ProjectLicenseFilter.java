package io.papermc.hangar.controller.extras.pagination.filters.projects;

import io.papermc.hangar.controller.extras.pagination.Filter;
import java.util.Arrays;
import java.util.Set;
import org.jdbi.v3.core.statement.SqlStatement;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.NativeWebRequest;

@Component
public class ProjectLicenseFilter implements Filter<ProjectLicenseFilter.ProjectLicenseFilterInstance, String[]> {

    private final ConversionService conversionService;

    @Autowired
    public ProjectLicenseFilter(final ConversionService conversionService) {
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

    @Override
    public String[] getValue(final NativeWebRequest webRequest) {
        return webRequest.getParameterValues(this.getSingleQueryParam());
    }

    @Override
    public @NotNull ProjectLicenseFilterInstance create(final NativeWebRequest webRequest) {
        return new ProjectLicenseFilterInstance(this.conversionService.convert(this.getValue(webRequest), String[].class));
    }

    static class ProjectLicenseFilterInstance implements Filter.FilterInstance {

        private final String[] licenses;

        public ProjectLicenseFilterInstance(final String[] licenses) {
            this.licenses = licenses;
        }

        @Override
        public void createSql(final StringBuilder sb, final SqlStatement<?> q) {
            sb.append(" AND p.license_type").append(" IN (");
            for (int i = 0; i < this.licenses.length; i++) {
                sb.append(":__licenses__").append(i);
                if (i + 1 != this.licenses.length) {
                    sb.append(',');
                }
                q.bind("__licenses__" + i, this.licenses[i]);
            }
            sb.append(')');
        }

        @Override
        public String toString() {
            return "ProjectLicenseFilterInstance{" +
                "licenses=" + Arrays.toString(this.licenses) +
                '}';
        }
    }
}
