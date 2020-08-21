package io.papermc.hangar.controller.interceptors;

import io.papermc.hangar.security.HangarAuthentication;
import io.papermc.hangar.service.project.ProjectService;
import io.papermc.hangar.model.NamedPermission;
import io.papermc.hangar.security.annotations.ProjectPermission;
import io.papermc.hangar.service.PermissionService;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

@Component
public class ProjectsInterceptor extends HandlerInterceptorAdapter {

    private final ProjectService projectService;
    private final PermissionService permissionService;

    @Autowired
    public ProjectsInterceptor(ProjectService projectService, PermissionService permissionService) {
        this.projectService = projectService;
        this.permissionService = permissionService;
    }

    @Override
    public boolean preHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) return true;
        HandlerMethod method = (HandlerMethod) handler;
        if (method.getMethod().isAnnotationPresent(ProjectPermission.class)) {
            if (!(SecurityContextHolder.getContext().getAuthentication() instanceof HangarAuthentication)) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
            }
            @SuppressWarnings("unchecked")
            Map<String, String> pathParamMap = (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
            if (!pathParamMap.keySet().equals(Set.of("author", "slug"))) {
                System.err.println("Can't have @ProjectPermission annotation on a request method without an 'author' and 'slug' path parameter");
                return false; // shouldnt happen. just don't place the annotations on methods that don't have these parameters
            }
            if (!(SecurityContextHolder.getContext().getAuthentication() instanceof HangarAuthentication)) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
            }
            long userId = ((HangarAuthentication) SecurityContextHolder.getContext().getAuthentication()).getUserId();
            Collection<NamedPermission> userProjectPermissions = permissionService.getProjectPermissions(userId, projectService.getProjectData(pathParamMap.get("author"), pathParamMap.get("slug")).getProject().getId()).toNamed();
            Collection<NamedPermission> requirePermissions = Arrays.asList(AnnotationUtils.getAnnotation(method.getMethod(), ProjectPermission.class).value());
            if (!userProjectPermissions.containsAll(requirePermissions)) {
                System.out.println("Required perms: " + requirePermissions.toString());
                System.out.println("User perms: " + userProjectPermissions.toString());
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
            }

        }
        return true;
    }
}
