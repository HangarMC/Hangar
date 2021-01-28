package io.papermc.hangar.service;

import io.papermc.hangar.controller.extras.ApiScope;
import io.papermc.hangar.controller.extras.HangarRequest;
import io.papermc.hangar.controller.extras.exceptions.HangarApiException;
import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.internal.ProjectDAO;
import io.papermc.hangar.db.dao.session.ApiSessionDAO;
import io.papermc.hangar.db.dao.session.HangarRequestDAO;
import io.papermc.hangar.model.Permission;
import io.papermc.hangar.model.db.OrganizationTable;
import io.papermc.hangar.model.db.projects.ProjectTable;
import io.papermc.hangar.service.internal.OrganizationService;
import io.papermc.hangar.util.AuthUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;

import javax.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;

@Service
public class AuthenticationService extends HangarService {

    private final HangarRequestDAO hangarRequestDAO;
    private final ApiSessionDAO apiSessionDAO;
    private final ProjectDAO projectDAO;
    private final PermissionService permissionService;
    private final VisibilityService visibilityService;
    private final OrganizationService organizationService;

    private final HttpServletRequest request;

    public AuthenticationService(HangarDao<HangarRequestDAO> hangarRequestDAO, HangarDao<ApiSessionDAO> apiSessionDAO, HangarDao<ProjectDAO> projectDAO, PermissionService permissionService, VisibilityService visibilityService, OrganizationService organizationService, HttpServletRequest request) {
        this.hangarRequestDAO = hangarRequestDAO.get();
        this.apiSessionDAO = apiSessionDAO.get();
        this.projectDAO = projectDAO.get();
        this.permissionService = permissionService;
        this.visibilityService = visibilityService;
        this.organizationService = organizationService;
        this.request = request;
    }

    @Bean
    @RequestScope
    public HangarRequest hangarRequest() {
        AuthUtils.AuthCredentials credentials = AuthUtils.parseAuthHeader(request, true);
        if (credentials.getSession() == null) {
            throw AuthUtils.unAuth("No session specified");
        }
        HangarRequest hangarRequest = hangarRequestDAO.createHangarRequest(credentials.getSession());
        if (hangarRequest == null) {
            throw AuthUtils.unAuth("Invalid session");
        }
        if (hangarRequest.getExpires().isBefore(OffsetDateTime.now())) {
            apiSessionDAO.delete(credentials.getSession());
            throw AuthUtils.unAuth("Api session expired");
        }
        return hangarRequest;
    }

    /**
     * For use in {@link org.springframework.security.access.prepost.PreAuthorize}.
     *
     * @param requiredPerms perms required for this controller method
     * @param apiScope scope of required perms
     * @return true if allowed, throws if not
     */
    public boolean handleApiRequest(Permission requiredPerms, ApiScope apiScope) {
        if (!checkPerms(requiredPerms, apiScope, hangarRequest.getUserId())) {
            throw new HangarApiException(HttpStatus.NOT_FOUND);
        }
        return true;
    }

    private boolean checkPerms(Permission requiredPerms, ApiScope apiScope, Long userId) {
        switch (apiScope.getType()) {
            case GLOBAL:
                return permissionService.getGlobalPermissions(userId).has(requiredPerms);
            case PROJECT:
                if ((StringUtils.isEmpty(apiScope.getOwner()) || StringUtils.isEmpty(apiScope.getSlug())) && apiScope.getId() == null) {
                    throw new IllegalArgumentException("Must have passed an (owner and slug) OR an ID to apiAction");
                }
                ProjectTable projectTable;
                Permission projectPermissions;
                if (apiScope.getId() != null) {
                    projectPermissions = permissionService.getProjectPermissions(userId, apiScope.getId());
                    projectTable = visibilityService.checkVisibility(projectDAO.getById(apiScope.getId()), projectPermissions);
                }
                else {
                    projectPermissions = permissionService.getProjectPermissions(userId, apiScope.getOwner(), apiScope.getSlug());
                    projectTable = visibilityService.checkVisibility(projectDAO.getBySlug(apiScope.getOwner(), apiScope.getSlug()), projectPermissions);
                }
                if (projectTable == null) {
                    throw new HangarApiException(HttpStatus.NOT_FOUND);
                }
                return projectPermissions.has(requiredPerms);
            case ORGANIZATION:
                if (StringUtils.isEmpty(apiScope.getOwner())) {
                    throw new IllegalArgumentException("Must have passed the owner to apiAction");
                }
                OrganizationTable organizationTable = organizationService.getOrganizationTable(apiScope.getOwner());
                if (organizationTable == null) {
                    throw new HangarApiException(HttpStatus.NOT_FOUND);
                }
                return permissionService.getOrganizationPermissions(userId, apiScope.getOwner()).has(requiredPerms);
            default:
                throw new HangarApiException(HttpStatus.BAD_REQUEST);
        }
    }
}
