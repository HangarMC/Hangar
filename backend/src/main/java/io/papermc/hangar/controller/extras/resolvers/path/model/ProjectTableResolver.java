package io.papermc.hangar.controller.extras.resolvers.path.model;

import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.db.projects.ProjectTable;
import io.papermc.hangar.service.internal.projects.ProjectService;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

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
    protected ProjectTable resolveParameter(final @NotNull String param) {
        final Long projectId = NumberUtils.createLong(param);
        if (projectId == null) {
            throw new HangarApiException(HttpStatus.NOT_FOUND);
        }
        return this.projectService.getProjectTable(projectId);
    }
}
