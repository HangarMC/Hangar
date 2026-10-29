package io.papermc.hangar.controller.extras.resolvers.path.model;

import io.papermc.hangar.model.db.versions.ProjectVersionTable;
import io.papermc.hangar.service.internal.versions.VersionService;
import io.papermc.hangar.util.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.NativeWebRequest;

@Component
public class ProjectVersionTableResolver extends HangarModelResolver<ProjectVersionTable> {

    private final VersionService versionService;

    @Autowired
    public ProjectVersionTableResolver(final VersionService versionService) {
        this.versionService = versionService;
    }

    @Override
    protected Class<ProjectVersionTable> modelType() {
        return ProjectVersionTable.class;
    }

    @Override
    protected ProjectVersionTable resolveParameter(final @NotNull String param, final NativeWebRequest request) {
        // Inside a project, the version string wins
        final Long projectId = request.getAttribute("projectId", NativeWebRequest.SCOPE_REQUEST) instanceof final Long id ? id : null;
        ProjectVersionTable projectVersionTable = projectId != null ? this.versionService.getProjectVersionTable(projectId, param) : null;
        if (projectVersionTable == null && this.supportsId(request) && StringUtils.isLong(param)) {
            projectVersionTable = this.versionService.getProjectVersionTable(Long.parseLong(param));
            if (projectVersionTable != null && projectId != null && projectVersionTable.getProjectId() != projectId) {
                projectVersionTable = null;
            }
        }

        if (projectVersionTable != null) {
            request.setAttribute("versionId", projectVersionTable.getId(), NativeWebRequest.SCOPE_REQUEST);
            return projectVersionTable;
        }

        return null;
    }
}
