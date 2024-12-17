package io.papermc.hangar.controller.extras.resolvers.path.model;

import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.db.versions.ProjectVersionTable;
import io.papermc.hangar.service.internal.versions.VersionService;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.NativeWebRequest;

@Component
public class ProjectVersionTableResolver extends HangarModelPathVarResolver<ProjectVersionTable> {

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
        if (NumberUtils.isParsable(param)) {
            final ProjectVersionTable projectVersionTable = this.versionService.getProjectVersionTable(Long.parseLong(param));
            if (projectVersionTable != null) {
                request.setAttribute("versionId", projectVersionTable.getId(), NativeWebRequest.SCOPE_REQUEST);
                return projectVersionTable;
            }
        }

        final Object projectId = request.getAttribute("projectId", NativeWebRequest.SCOPE_REQUEST);
        if (!(projectId instanceof final Long projectIdLong)) {
            throw new HangarApiException(HttpStatus.NOT_FOUND);
        }

        return this.versionService.getProjectVersionTable(projectIdLong, param);
    }
}
