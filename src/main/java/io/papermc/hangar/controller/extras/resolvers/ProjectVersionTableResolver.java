package io.papermc.hangar.controller.extras.resolvers;

import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.internal.table.versions.ProjectVersionsDAO;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.db.versions.ProjectVersionTable;
import io.papermc.hangar.service.internal.versions.VersionService;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.servlet.mvc.method.annotation.PathVariableMethodArgumentResolver;

@Component
public class ProjectVersionTableResolver extends PathVariableMethodArgumentResolver {

    private final ProjectVersionsDAO projectVersionsDAO;
    private final VersionService versionService;

    @Autowired
    public ProjectVersionTableResolver(HangarDao<ProjectVersionsDAO> projectVersionsDAO, VersionService versionService) {
        this.projectVersionsDAO = projectVersionsDAO.get();
        this.versionService = versionService;
    }

    @Override
    public boolean supportsParameter(@NotNull MethodParameter parameter) {
        return super.supportsParameter(parameter) && parameter.getParameterType().equals(ProjectVersionTable.class);
    }

    @Override
    protected Object resolveName(@NotNull String name, @NotNull MethodParameter parameter, @NotNull NativeWebRequest request) throws Exception {
        Long versionId = NumberUtils.createLong((String) super.resolveName(name, parameter, request));
        if (versionId == null) {
            throw new HangarApiException(HttpStatus.NOT_FOUND);
        }
        if (parameter.hasParameterAnnotation(NoCache.class)) {
            return projectVersionsDAO.getProjectVersionTable(versionId);
        } else {
            return versionService.getProjectVersionTable(versionId);
        }
    }
}
