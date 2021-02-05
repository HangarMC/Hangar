package io.papermc.hangar.service;

import io.papermc.hangar.config.hangar.HangarConfig;
import io.papermc.hangar.controller.extras.ApiScope;
import io.papermc.hangar.controller.extras.HangarApiRequest;
import io.papermc.hangar.controller.extras.HangarRequest;
import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.internal.table.ProjectDAO;
import io.papermc.hangar.db.dao.internal.table.auth.ApiKeyDAO;
import io.papermc.hangar.db.dao.internal.table.auth.ApiSessionDAO;
import io.papermc.hangar.db.dao.session.HangarRequestDAO;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.api.auth.ApiSession;
import io.papermc.hangar.model.common.Permission;
import io.papermc.hangar.model.common.roles.GlobalRole;
import io.papermc.hangar.model.db.OrganizationTable;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.model.db.auth.ApiKeyTable;
import io.papermc.hangar.model.db.auth.ApiSessionTable;
import io.papermc.hangar.model.db.projects.ProjectTable;
import io.papermc.hangar.security.HangarAuthenticationToken;
import io.papermc.hangar.service.internal.OrganizationService;
import io.papermc.hangar.service.internal.UserService;
import io.papermc.hangar.service.internal.roles.GlobalRoleService;
import io.papermc.hangar.util.AuthUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;

import javax.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class AuthenticationService extends HangarService {

    private static final String UUID_REGEX = "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}";
    private static final Pattern API_KEY_PATTERN = Pattern.compile("(" + UUID_REGEX + ").(" + UUID_REGEX + ")");

    private final HangarConfig hangarConfig;
    private final HangarRequestDAO hangarRequestDAO;
    private final ApiSessionDAO apiSessionDAO;
    private final ProjectDAO projectDAO;
    private final ApiKeyDAO apiKeyDAO;
    private final PermissionService permissionService;
    private final VisibilityService visibilityService;
    private final OrganizationService organizationService;
    private final UserService userService;
    private final GlobalRoleService globalRoleService;

    private final HttpServletRequest request;

    public AuthenticationService(HangarConfig hangarConfig, HangarDao<HangarRequestDAO> hangarRequestDAO, HangarDao<ApiSessionDAO> apiSessionDAO, HangarDao<ProjectDAO> projectDAO, HangarDao<ApiKeyDAO> apiKeyDAO, PermissionService permissionService, VisibilityService visibilityService, OrganizationService organizationService, UserService userService, GlobalRoleService globalRoleService, HttpServletRequest request) {
        this.hangarConfig = hangarConfig;
        this.hangarRequestDAO = hangarRequestDAO.get();
        this.apiSessionDAO = apiSessionDAO.get();
        this.projectDAO = projectDAO.get();
        this.apiKeyDAO = apiKeyDAO.get();
        this.permissionService = permissionService;
        this.visibilityService = visibilityService;
        this.organizationService = organizationService;
        this.userService = userService;
        this.globalRoleService = globalRoleService;
        this.request = request;
    }

    @Bean
    @RequestScope
    public HangarApiRequest hangarApiRequest() {
        AuthUtils.AuthCredentials credentials = AuthUtils.parseAuthHeader(request, true);
        if (credentials.getSession() == null) {
            throw AuthUtils.unAuth("No session specified");
        }
        HangarApiRequest hangarApiRequest = hangarRequestDAO.createHangarRequest(credentials.getSession());
        if (hangarApiRequest == null) {
            throw AuthUtils.unAuth("Invalid session");
        }
        if (hangarApiRequest.getExpires().isBefore(OffsetDateTime.now())) {
            apiSessionDAO.delete(credentials.getSession());
            throw AuthUtils.unAuth("Api session expired");
        }
        return hangarApiRequest;
    }

    @Bean
    @RequestScope
    public HangarRequest hangarRequest() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserTable userTable = null;
        Long userId = null;
        if (authentication instanceof HangarAuthenticationToken) {
            HangarAuthenticationToken hangarAuthentication = (HangarAuthenticationToken) authentication;
            userTable = userService.getUserTable(hangarAuthentication.getUserId());
            if (userTable != null) userId = userTable.getUserId();
        }
        return new HangarRequest(userTable, permissionService.getGlobalPermissions(userId));
    }

    /**
     * For use in {@link org.springframework.security.access.prepost.PreAuthorize}.
     *
     * @param requiredPerms perms required for this controller method
     * @param apiScope scope of required perms
     * @return true if allowed, throws if not
     */
    public boolean handleApiRequest(Permission requiredPerms, ApiScope apiScope) {
        if (!checkPerms(requiredPerms, apiScope, hangarApiRequest.getUserId())) {
            throw new HangarApiException(HttpStatus.NOT_FOUND, "Resource NOT FOUND");
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
                    throw new HangarApiException(HttpStatus.NOT_FOUND, "Resource NOT FOUND");
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

    public ApiSession authenticateDev() {
        if (hangarConfig.fakeUser.isEnabled()) {
            hangarConfig.checkDebug();
            OffsetDateTime sessionExpiration = AuthUtils.expiration(hangarConfig.api.session.getExpiration(), null);
            String uuidToken = UUID.randomUUID().toString();

            ApiSessionTable apiSessionTable = ApiSessionTable.createUserKey(uuidToken, hangarConfig.fakeUser.getId(), sessionExpiration);
            saveApiSessionTable(apiSessionTable);

            return new ApiSession(apiSessionTable.getToken(), apiSessionTable.getExpires(), ApiSession.SessionType.DEV);
        } else {
            throw new HangarApiException(HttpStatus.FORBIDDEN);
        }
    }

    public ApiSession authenticateKeyPublic(Long expiresIn) {
        OffsetDateTime sessionExpiration = AuthUtils.expiration(hangarConfig.api.session.getExpiration(), expiresIn);
        OffsetDateTime publicSessionExpiration = AuthUtils.expiration(hangarConfig.api.session.getPublicExpiration(), expiresIn);
        String uuidToken = UUID.randomUUID().toString();

        AuthUtils.AuthCredentials credentials = AuthUtils.parseAuthHeader(false);
        ApiSession.SessionType sessionType;
        ApiSessionTable apiSession;
        if (credentials.getApiKey() != null) {
            if (!API_KEY_PATTERN.matcher(credentials.getApiKey()).find()) {
                throw AuthUtils.unAuth("No valid apikey parameter found in Authorization");
            }
            if (sessionExpiration == null) {
                throw new HangarApiException(HttpStatus.BAD_REQUEST, "The requested expiration can't be used");
            }
            // I THINK that is how its setup, couldn't really figure it out via ore
            String identifier = credentials.getApiKey().split("\\.")[0];
            String token = credentials.getApiKey().split("\\.")[1];
            ApiKeyTable apiKeysTable = apiKeyDAO.findApiKey(identifier, token);
            if (apiKeysTable == null) {
                throw AuthUtils.unAuth("Invalid api key");
            }
            apiSession = ApiSessionTable.fromApiKey(uuidToken, apiKeysTable, sessionExpiration);
            sessionType = ApiSession.SessionType.KEY;
        } else {
            if (publicSessionExpiration == null) {
                throw new HangarApiException(HttpStatus.BAD_REQUEST, "The requested expiration can't be used");
            }
            apiSession = ApiSessionTable.createPublicKey(uuidToken, publicSessionExpiration);
            sessionType = ApiSession.SessionType.PUBLIC;
        }

        saveApiSessionTable(apiSession);
        return new ApiSession(apiSession.getToken(), apiSession.getExpires(), sessionType);
    }

    public ApiSession authenticateUser(long userId) {
        OffsetDateTime sessionExpiration = AuthUtils.expiration(hangarConfig.api.session.getExpiration(), null);
        String uuidToken = UUID.randomUUID().toString();

        ApiSessionTable apiSession = ApiSessionTable.createUserKey(uuidToken, userId, sessionExpiration);
        saveApiSessionTable(apiSession);
        return new ApiSession(apiSession.getToken(), apiSession.getExpires(), ApiSession.SessionType.USER);
    }

    private void saveApiSessionTable(ApiSessionTable apiSessionTable) {
        apiSessionDAO.insert(apiSessionTable);
    }

    public UserTable loginAsFakeUser() {
        String userName = hangarConfig.fakeUser.getUsername();
        UserTable userTable = userService.getUserTable(userName);
        if (userTable == null) {
            userTable = new UserTable(
                    hangarConfig.fakeUser.getId(),
                    hangarConfig.fakeUser.getName(),
                    userName,
                    hangarConfig.fakeUser.getEmail(),
                    List.of(),
                    false,
                    Locale.ENGLISH.toLanguageTag()
            );

            userTable = userService.insertUser(userTable);

            globalRoleService.addRole(GlobalRole.HANGAR_ADMIN.create(null, userTable.getId(), true));
        }
        setAuthenticatedUser(userTable);
        return userTable;
    }

    public void setAuthenticatedUser(UserTable user) {
        // TODO properly do auth, remember me shit too
//        Authentication auth = new HangarAuthenticationToken(List.of(new SimpleGrantedAuthority("ROLE_USER")), user.getName(), user.getId());
//        authenticationManager.authenticate(auth);
//        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
