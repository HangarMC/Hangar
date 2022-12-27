package io.papermc.hangar.controller.extras.pagination.filters.versions;

import io.papermc.hangar.controller.extras.pagination.Filter;
import io.papermc.hangar.controller.extras.pagination.filters.versions.VersionTagFilter.VersionTagFilterInstance;
import io.papermc.hangar.exceptions.HangarApiException;
import java.util.Set;
import org.jdbi.v3.core.statement.SqlStatement;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.request.NativeWebRequest;

@Component
public class VersionTagFilter implements Filter<VersionTagFilterInstance> {

    @Override
    public Set<String> getQueryParamNames() {
        return Set.of("vTag");
    }

    @Override
    public String getDescription() {
        return "A version tag to filter for";
    }

    @Override
    public @NotNull VersionTagFilterInstance create(final NativeWebRequest webRequest) {
        final MultiValueMap<String, String> versionTags = new LinkedMultiValueMap<>();
        for (final String tag : webRequest.getParameterValues(this.getSingleQueryParam())) {
            if (!tag.contains(":")) {
                throw new HangarApiException(HttpStatus.BAD_REQUEST, "Must specify a version. e.g. Paper:1.14");
            }
            final String[] splitTag = tag.split(":", 2);
            versionTags.add(splitTag[0], splitTag[1]);
        }
        return new VersionTagFilterInstance(versionTags);
    }

    static class VersionTagFilterInstance implements Filter.FilterInstance {

        private final MultiValueMap<String, String> versionTags;

        VersionTagFilterInstance(final MultiValueMap<String, String> versionTags) {
            this.versionTags = versionTags;
        }

        @Override
        public void createSql(final StringBuilder sb, final SqlStatement<?> q) {
            sb.append(" AND (");
            this.versionTags.forEach((name, versions) -> {
                q.bind("__vTag_name_" + name, name);
                for (int i = 0; i < versions.size(); i++) {
                    sb.append(":__vTag_name_").append(name).append("_v_").append(i).append(" = ANY(SELECT unnest(vtsq.data) WHERE vtsq.name = :__vTag_name_").append(name).append(")");
                    q.bind("__vTag_name_" + name + "_v_" + i, versions.get(i));
                    if (i + 1 != versions.size()) {
                        sb.append(" OR ");
                    }
                }
            });
            sb.append(")");
        }

        @Override
        public String toString() {
            return "VersionTagFilterInstance{" +
                "versionTags=" + this.versionTags +
                '}';
        }
    }
}
