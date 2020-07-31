package me.minidigger.hangar.controller.interceptors;

import me.minidigger.hangar.model.viewhelpers.ProjectData;
import me.minidigger.hangar.security.HangarAuthentication;
import me.minidigger.hangar.security.annotations.ProjectPermission;
import me.minidigger.hangar.service.project.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Set;

@Component
public class ProjectsInterceptor extends HandlerInterceptorAdapter {

    private final ProjectService projectService;

    @Autowired
    public ProjectsInterceptor(ProjectService projectService) {
        this.projectService = projectService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HandlerMethod method = (HandlerMethod) handler;
        if (method.getMethod().isAnnotationPresent(ProjectPermission.class)) {
            if (!(SecurityContextHolder.getContext().getAuthentication() instanceof HangarAuthentication)) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
            }
            if (!request.getParameterMap().keySet().containsAll(Set.of("author", "slug"))) return false; // shouldnt happen. just don't place the annotations on methods that don't have these parameters
            long projectId = projectService.getProjectData(request.getParameter("author"), request.getParameter("slug")).getProject().getId();

            HangarAuthentication hangerAuth = (HangarAuthentication) SecurityContextHolder.getContext().getAuthentication();
            hangerAuth.setProjectId(projectId);

        }
        return true;
    }
}
