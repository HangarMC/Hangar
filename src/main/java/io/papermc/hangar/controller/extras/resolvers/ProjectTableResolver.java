package io.papermc.hangar.controller.extras.resolvers;

import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.internal.table.projects.ProjectsDAO;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.db.projects.ProjectTable;
import io.papermc.hangar.service.internal.projects.ProjectService;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.servlet.mvc.method.annotation.PathVariableMethodArgumentResolver;

@Component
public class ProjectTableResolver extends PathVariableMethodArgumentResolver {

    private final ProjectsDAO projectsDAO;
    private final ProjectService projectService;

    @Autowired
    public ProjectTableResolver(HangarDao<ProjectsDAO> projectsDAO, ProjectService projectService) {
        this.projectsDAO = projectsDAO.get();
        this.projectService = projectService;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return super.supportsParameter(parameter) && parameter.getParameterType().equals(ProjectTable.class);
    }

    @Override
    protected Object resolveName(String name, MethodParameter parameter, NativeWebRequest request) throws Exception {
        Long projectId = NumberUtils.createLong((String) super.resolveName(name, parameter, request));
        if (projectId == null) {
            throw new HangarApiException(HttpStatus.NOT_FOUND);
        }
        if (parameter.hasParameterAnnotation(NoCache.class)) {
            return projectsDAO.getById(projectId);
        } else {
            return projectService.getProjectTable(projectId);
        }
    }
}
