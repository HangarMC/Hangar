package io.papermc.hangar.controller.extras.pagination.filters.projects;

import io.papermc.hangar.controller.extras.pagination.Filter;
import java.util.Locale;
import java.util.Set;
import org.jdbi.v3.core.statement.SqlStatement;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.NativeWebRequest;

@Component
public class ProjectMemberFilter implements Filter<ProjectMemberFilter.ProjectMemberFilterInstance, String> {

    @Override
    public Set<String> getQueryParamNames() {
        return Set.of("member");
    }

    @Override
    public String getDescription() {
        return "The member of the project";
    }

    @Override
    public String getValue(final NativeWebRequest webRequest) {
        return webRequest.getParameter(this.getSingleQueryParam());
    }

    @Override
    public @NotNull ProjectMemberFilterInstance create(final NativeWebRequest webRequest) {
        return new ProjectMemberFilterInstance(this.getValue(webRequest));
    }

    static class ProjectMemberFilterInstance implements Filter.FilterInstance {

        private final String memberName;

        ProjectMemberFilterInstance(final String memberName) {
            this.memberName = memberName;
        }

        @Override
        public void createSql(final StringBuilder sb, final SqlStatement<?> q) {
            sb.append(" AND :memberName = ANY(hp.project_member_names)");
            q.bind("memberName", this.memberName.toLowerCase(Locale.ROOT));
        }

        @Override
        public void createMeili(final StringBuilder sb) {
            sb.append("memberNames IN [");
            sb.append(this.memberName.toLowerCase(Locale.ROOT));
            sb.append("]");
        }

        @Override
        public String toString() {
            return "ProjectMemberFilterInstance{" +
                "memberName='" + this.memberName + '\'' +
                '}';
        }
    }
}
