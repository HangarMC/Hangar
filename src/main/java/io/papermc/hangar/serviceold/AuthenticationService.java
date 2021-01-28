package io.papermc.hangar.serviceold;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.papermc.hangar.config.hangar.HangarConfig;
import io.papermc.hangar.controller.extras.ApiScope;
import io.papermc.hangar.controller.extras.exceptions.HangarApiException;
import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.daoold.ApiKeyDao;
import io.papermc.hangar.db.daoold.ProjectDao;
import io.papermc.hangar.db.daoold.UserDao;
import io.papermc.hangar.db.daoold.api.SessionsDao;
import io.papermc.hangar.db.modelold.ApiKeysTable;
import io.papermc.hangar.db.modelold.ApiSessionsTable;
import io.papermc.hangar.db.modelold.OrganizationsTable;
import io.papermc.hangar.db.modelold.ProjectsTable;
import io.papermc.hangar.db.modelold.UsersTable;
import io.papermc.hangar.model.Permission;
import io.papermc.hangar.model.Role;
import io.papermc.hangar.modelold.ApiAuthInfo;
import io.papermc.hangar.modelold.generated.ApiSessionResponse;
import io.papermc.hangar.modelold.generated.SessionType;
import io.papermc.hangar.security.HangarAuthentication;
import io.papermc.hangar.service.PermissionService;
import io.papermc.hangar.service.VisibilityService;
import io.papermc.hangar.serviceold.sso.ChangeAvatarToken;
import io.papermc.hangar.util.AuthUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service("oldAuthenticationService")
public class AuthenticationService extends HangarService {


    private final HangarConfig hangarConfig;
    private final HangarDao<UserDao> userDao;
    private final HangarDao<SessionsDao> sessionsDao;
    private final HangarDao<ApiKeyDao> apiKeyDao;
    private final HangarDao<ProjectDao> projectDao;
    private final AuthenticationManager authenticationManager;
    private final RoleService roleService;
    private final PermissionService permissionService;
    private final VisibilityService visibilityService;
    private final OrgService orgService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private final HttpServletRequest request;
    private final Supplier<Optional<UsersTable>> currentUser;

    private static final Pattern API_ROUTE_PATTERN = Pattern.compile("^/api/(?!old).+");
    private static final String UUID_REGEX = "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}";
    private static final Pattern API_KEY_PATTERN = Pattern.compile("(" + UUID_REGEX + ").(" + UUID_REGEX + ")");

    @Autowired
    public AuthenticationService(HangarConfig hangarConfig, HangarDao<UserDao> userDao, HangarDao<SessionsDao> sessionsDao, HangarDao<ApiKeyDao> apiKeyDao, HangarDao<ProjectDao> projectDao, AuthenticationManager authenticationManager, RoleService roleService, PermissionService permissionService, VisibilityService visibilityService, OrgService orgService, RestTemplate restTemplate, ObjectMapper objectMapper, HttpServletRequest request, Supplier<Optional<UsersTable>> currentUser) {
        this.hangarConfig = hangarConfig;
        this.userDao = userDao;
        this.sessionsDao = sessionsDao;
        this.apiKeyDao = apiKeyDao;
        this.projectDao = projectDao;
        this.authenticationManager = authenticationManager;
        this.roleService = roleService;
        this.permissionService = permissionService;
        this.visibilityService = visibilityService;
        this.orgService = orgService;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.request = request;
        this.currentUser = currentUser;
    }


    @Bean
    @RequestScope
    public ApiAuthInfo apiAuthInfo() {
        Matcher apiMatcher = API_ROUTE_PATTERN.matcher(request.getRequestURI());
        if (apiMatcher.matches()) { // if api method
            AuthUtils.AuthCredentials credentials = AuthUtils.parseAuthHeader(request, true);
            if (credentials.getSession() == null) {
                throw AuthUtils.unAuth("No session specified");
            }
            ApiAuthInfo info = apiKeyDao.get().getApiAuthInfo(credentials.getSession());
            if (info == null) {
                throw AuthUtils.unAuth("Invalid session");
            }
            if (info.getExpires().isBefore(OffsetDateTime.now())) {
                sessionsDao.get().delete(credentials.getSession());
                throw AuthUtils.unAuth("Api session expired");
            }
            return info;
        }
        return null;
    }

    // TODO remove once all of old v1 is gone
    public boolean authV1ApiRequest(Permission perms, ApiScope apiScope) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "GET RID OF THIS");
    }

    public boolean authApiRequest(Permission perms, ApiScope apiScope) {
        //noinspection SpringConfigurationProxyMethods
        boolean hasPerms = checkPerms(perms, apiScope, apiAuthInfo().getUserId());
        if (!hasPerms) {
            throw new HangarApiException(HttpStatus.NOT_FOUND);
        }
        return true;
    }

    private boolean checkPerms(Permission perms, ApiScope apiScope, Long userId) {
        switch (apiScope.getType()) {
            case GLOBAL:
                return permissionService.getGlobalPermissions(userId).has(perms);
            case PROJECT:
                if ((StringUtils.isEmpty(apiScope.getOwner()) || StringUtils.isEmpty(apiScope.getSlug())) && apiScope.getId() == null) {
                    throw new IllegalArgumentException("Must have passed an (owner and slug) OR an ID to apiAction");
                }
                ProjectsTable projectsTable;
                Permission projectPermissions;
                if (apiScope.getId() != null) {
                    projectPermissions = permissionService.getProjectPermissions(userId, apiScope.getId());
                    projectsTable = visibilityService.checkVisibility(projectDao.get().getById(apiScope.getId()), projectPermissions);
                }
                else {
                    projectPermissions = permissionService.getProjectPermissions(userId, apiScope.getOwner(), apiScope.getSlug());
                    projectsTable = visibilityService.checkVisibility(projectDao.get().getBySlug(apiScope.getOwner(), apiScope.getSlug()), projectPermissions);
                }
                if (projectsTable == null) {
                    throw new HangarApiException(HttpStatus.NOT_FOUND);
                }
                return projectPermissions.has(perms);
            case ORGANIZATION:
                if (StringUtils.isEmpty(apiScope.getOwner())) {
                    throw new IllegalArgumentException("Must have passed the owner to apiAction");
                }
                OrganizationsTable organizationsTable = orgService.getOrganization(apiScope.getOwner());
                if (organizationsTable == null) {
                    throw new HangarApiException(HttpStatus.NOT_FOUND);
                }
                return permissionService.getOrganizationPermissions(userId, apiScope.getOwner()).has(perms);
            default:
                throw new HangarApiException(HttpStatus.BAD_REQUEST);
        }
    }

    public ApiSessionResponse authenticateDev() {
        if (hangarConfig.fakeUser.isEnabled()) {
            hangarConfig.checkDebug();
            OffsetDateTime sessionExpiration = AuthUtils.expiration(hangarConfig.api.session.getExpiration(), null);
            String uuidToken = UUID.randomUUID().toString();

            ApiSessionsTable apiSession = new ApiSessionsTable(uuidToken, null, hangarConfig.fakeUser.getId(), sessionExpiration);
            saveSession(apiSession);

            return new ApiSessionResponse(apiSession.getToken(), apiSession.getExpires(), SessionType.DEV);
        } else {
            throw new HangarApiException(HttpStatus.FORBIDDEN);
        }
    }

    public ApiSessionResponse authenticateKeyPublic(Long expiresIn) {
        OffsetDateTime sessionExpiration = AuthUtils.expiration(hangarConfig.api.session.getExpiration(), expiresIn);
        OffsetDateTime publicSessionExpiration = AuthUtils.expiration(hangarConfig.api.session.getPublicExpiration(), expiresIn);
        String uuidToken = UUID.randomUUID().toString();

        AuthUtils.AuthCredentials credentials = AuthUtils.parseAuthHeader(false);
        SessionType sessionType;
        ApiSessionsTable apiSession;
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
            ApiKeysTable apiKeysTable = apiKeyDao.get().findApiKey(identifier, token);
            if (apiKeysTable == null) {
                throw AuthUtils.unAuth("Invalid api key");
            }
            apiSession = new ApiSessionsTable(uuidToken, apiKeysTable.getId(), apiKeysTable.getOwnerId(), sessionExpiration);
            sessionType = SessionType.KEY;
        } else {
            if (publicSessionExpiration == null) {
                throw new HangarApiException(HttpStatus.BAD_REQUEST, "The requested expiration can't be used");
            }
            apiSession = new ApiSessionsTable(uuidToken, null, null, publicSessionExpiration);
            sessionType = SessionType.PUBLIC;
        }

        sessionsDao.get().insert(apiSession);

        return new ApiSessionResponse(apiSession.getToken(), apiSession.getExpires(), sessionType);
    }

    public ApiSessionResponse authenticateUser(long userId) {
        OffsetDateTime sessionExpiration = AuthUtils.expiration(hangarConfig.api.session.getExpiration(), null);
        String uuidToken = UUID.randomUUID().toString();

        ApiSessionsTable apiSession = new ApiSessionsTable(uuidToken, null, userId, sessionExpiration);
        saveSession(apiSession);

        return new ApiSessionResponse(apiSession.getToken(), apiSession.getExpires(), SessionType.USER);
    }

    private ApiSessionsTable saveSession(ApiSessionsTable apiSession) {
        return sessionsDao.get().insert(apiSession);
    }

    public UsersTable loginAsFakeUser() {
        String username = hangarConfig.fakeUser.getUsername();
        UsersTable userEntry = userDao.get().getByName(username);
        if (userEntry == null) {
            userEntry = new UsersTable();
            userEntry.setEmail(hangarConfig.fakeUser.getEmail());
            userEntry.setFullName(hangarConfig.fakeUser.getName());
            userEntry.setName(hangarConfig.fakeUser.getUsername());
            userEntry.setId(hangarConfig.fakeUser.getId());
            userEntry.setReadPrompts(List.of());

            userEntry = userDao.get().insert(userEntry);

            roleService.addGlobalRole(userEntry.getId(), Role.HANGAR_ADMIN.getRoleId());
        }
        setAuthenticatedUser(userEntry);
        return userEntry;
    }

    public void setAuthenticatedUser(UsersTable user) {
        // TODO properly do auth, remember me shit too
        Authentication auth = new HangarAuthentication(List.of(new SimpleGrantedAuthority("ROLE_USER")), user.getName(), user.getId());
        authenticationManager.authenticate(auth);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    public URI changeAvatarUri(String requester, String organization) throws JsonProcessingException {
        ChangeAvatarToken token = getChangeAvatarToken(requester, organization);
        UriComponentsBuilder uriComponents = UriComponentsBuilder.fromHttpUrl(hangarConfig.getAuthUrl());
        uriComponents.path("/accounts/user/{organization}/change-avatar/").queryParam("key", token.getSignedData());
        return uriComponents.build(organization);
    }

    public ChangeAvatarToken getChangeAvatarToken(String requester, String organization) throws JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> bodyMap = new LinkedMultiValueMap<>();
        bodyMap.add("api-key", hangarConfig.sso.getApiKey());
        bodyMap.add("request_username", requester);
        ChangeAvatarToken token;
        token = objectMapper.treeToValue(restTemplate.postForObject(hangarConfig.security.api.getUrl() + "/api/users/" + organization + "/change-avatar-token/", new HttpEntity<>(bodyMap, headers), ObjectNode.class), ChangeAvatarToken.class);
        return token;
    }

}
