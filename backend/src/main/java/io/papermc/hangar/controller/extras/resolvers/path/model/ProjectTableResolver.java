package io.papermc.hangar.controller.extras.resolvers.path.model;

import io.papermc.hangar.model.db.projects.ProjectTable;
import io.papermc.hangar.service.internal.projects.ProjectService;
import io.papermc.hangar.util.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.NativeWebRequest;

@Component
public class ProjectTableResolver extends HangarModelResolver<ProjectTable> {

    private final ProjectService projectService;

    @Autowired
    public ProjectTableResolver(final ProjectService projectService) {
        this.projectService = projectService;
    }

    @Override
    protected Class<ProjectTable> modelType() {
        return ProjectTable.class;
    }

    @Override
    protected ProjectTable resolveParameter(final @NotNull String param, final NativeWebRequest request) {
        ProjectTable projectTable = null;
        if (this.supportsId(request) && StringUtils.isLong(param)) {
            projectTable = this.projectService.getProjectTable(Long.parseLong(param));
        }

        if (projectTable == null) {
            projectTable = this.projectService.getProjectTable(param);
        }

        if (projectTable != null) {
            request.setAttribute("projectId", projectTable.getId(), NativeWebRequest.SCOPE_REQUEST);
            return projectTable;
        }

        return null;
    }
}
