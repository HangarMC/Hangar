package io.papermc.hangar.controller.extras.resolvers.path.model;

import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.db.projects.ProjectTable;
import io.papermc.hangar.service.internal.projects.ProjectService;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.NativeWebRequest;

@Component
public class ProjectTableResolver extends HangarModelPathVarResolver<ProjectTable> {

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
        if (NumberUtils.isParsable(param)) {
            final ProjectTable projectTable = this.projectService.getProjectTable(Long.parseLong(param));
            if (projectTable != null) {
                request.setAttribute("projectId", projectTable.getId(), NativeWebRequest.SCOPE_REQUEST);
                return projectTable;
            }
        }

        final ProjectTable projectTable = this.projectService.getProjectTable(param);
        if (projectTable != null) {
            request.setAttribute("projectId", projectTable.getId(), NativeWebRequest.SCOPE_REQUEST);
            return projectTable;
        }
        throw new HangarApiException(HttpStatus.NOT_FOUND);
    }
}
