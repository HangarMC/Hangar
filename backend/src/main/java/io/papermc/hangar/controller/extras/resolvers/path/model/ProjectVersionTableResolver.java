package io.papermc.hangar.controller.extras.resolvers.path.model;

import io.papermc.hangar.model.db.versions.ProjectVersionTable;
import io.papermc.hangar.service.internal.versions.VersionService;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
    protected ProjectVersionTable resolveParameter(final @NotNull String param) {
        final Long versionId = NumberUtils.createLong(param);
        if (versionId == null) {
            return null;
        }
        return this.versionService.getProjectVersionTable(versionId);
    }
}
