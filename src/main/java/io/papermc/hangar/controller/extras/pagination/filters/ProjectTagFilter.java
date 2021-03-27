package io.papermc.hangar.controller.extras.pagination.filters;

import io.papermc.hangar.controller.extras.pagination.Filter;
import io.papermc.hangar.controller.extras.pagination.filters.ProjectTagFilter.ProjectTagFilterInstance;
import org.jdbi.v3.core.statement.SqlStatement;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.ArrayList;
import java.util.List;

@Component
public class ProjectTagFilter implements Filter<ProjectTagFilterInstance> {

    @Override
    public String getQueryParamName() {
        return "tag";
    }

    @NotNull
    @Override
    public ProjectTagFilterInstance create(NativeWebRequest webRequest) {
        List<String> tagNames = new ArrayList<>();
        MultiValueMap<String, String> tagNamesAndData = new LinkedMultiValueMap<>();
        for (String tag : webRequest.getParameterValues(getQueryParamName())) {
            String[] splitTag = tag.split(":", 2);
            if (splitTag.length == 1) {
                tagNames.add(splitTag[0]);
            } else {
                tagNamesAndData.add(splitTag[0], splitTag[1]);
            }
        }
        return new ProjectTagFilterInstance(tagNames, tagNamesAndData);
    }

    static class ProjectTagFilterInstance implements FilterInstance {

        private final List<String> tagNames;
        private final MultiValueMap<String, String> tagNamesAndData;

        ProjectTagFilterInstance(List<String> tagNames, MultiValueMap<String, String> tagNamesAndData) {
            this.tagNames = tagNames;
            this.tagNamesAndData = tagNamesAndData;
        }

        @Override
        public void createSql(StringBuilder sb, SqlStatement<?> q) {
            sb.append(" AND ")
                    .append("exists(")
                    .append("SELECT pv.tag_name")
                    .append(" FROM jsonb_to_recordset(hp.promoted_versions) AS pv(tag_name text, tag_version text[])")
                    .append(" WHERE ");
            if (!tagNames.isEmpty()) {
                sb.append("pv.tag_name IN (");
                for (int i = 0; i < tagNames.size(); i++) {
                    sb.append(":__tagName__").append(i);
                    if (i + 1 != tagNames.size()) {
                        sb.append(",");
                    }
                    q.bind("__tagName__" + i, tagNames.get(i));
                }
                sb.append(")");
            }
            if (!tagNamesAndData.isEmpty()) {
                if (!tagNames.isEmpty()) {
                    sb.append(" AND ");
                }
                tagNamesAndData.forEach((name, data) -> {
                    sb.append("pv.tag_name = ").append(":__tagNameData_name_").append(name).append("__");
                    q.bind("__tagNameData_name_" + name + "__", name);
                    sb.append(" AND (");
                    for (int i = 0; i < data.size(); i++) {
                        sb.append(":__tagNameData_name_").append(name).append("_data_").append(i).append("__").append(" = ANY(pv.tag_version)");
                        q.bind("__tagNameData_name_" + name + "_data_" + i + "__", data.get(i));
                        if (i + 1 != data.size()) {
                            sb.append(" OR ");
                        }
                    }
                    sb.append(") ");
                });
            }
            sb.append(")");
        }
    }
}
