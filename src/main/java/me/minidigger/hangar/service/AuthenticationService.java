package me.minidigger.hangar.service;

import me.minidigger.hangar.config.hangar.HangarConfig;
import me.minidigger.hangar.db.dao.ApiKeyDao;
import me.minidigger.hangar.db.dao.HangarDao;
import me.minidigger.hangar.db.dao.UserDao;
import me.minidigger.hangar.db.dao.api.SessionsDao;
import me.minidigger.hangar.db.model.ApiKeysTable;
import me.minidigger.hangar.db.model.ApiSessionsTable;
import me.minidigger.hangar.db.model.UsersTable;
import me.minidigger.hangar.model.ApiAuthInfo;
import me.minidigger.hangar.model.Permission;
import me.minidigger.hangar.model.Role;
import me.minidigger.hangar.model.generated.ApiSessionResponse;
import me.minidigger.hangar.model.generated.SessionProperties;
import me.minidigger.hangar.model.generated.SessionType;
import me.minidigger.hangar.security.HangarAuthentication;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static me.minidigger.hangar.util.AuthUtils.unAuth;

@Service
public class AuthenticationService {

    private final HangarConfig hangarConfig;
    private final HangarDao<UserDao> userDao;
    private final HangarDao<SessionsDao> sessionsDao;
    private final HangarDao<ApiKeyDao> apiKeyDao;
    private final AuthenticationManager authenticationManager;
    private final RoleService roleService;
    private final PermissionService permissionService;
    private final SsoService ssoService;

    private static final Pattern API_KEY_HEADER_PATTERN = Pattern.compile("(?<=apikey=\").*(?=\")", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern SESSION_HEADER_PATTERN = Pattern.compile("(?<=session=\").*(?=\")", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final String UUID_REGEX = "[0-9a-fA-F]{8}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{12}";
    private static final Pattern API_KEY_PATTERN = Pattern.compile("(" + UUID_REGEX + ").(" + UUID_REGEX + ")");

    @Autowired
    public AuthenticationService(HangarConfig hangarConfig, HangarDao<UserDao> userDao, HangarDao<SessionsDao> sessionsDao, HangarDao<ApiKeyDao> apiKeyDao, AuthenticationManager authenticationManager, RoleService roleService, PermissionService permissionService, SsoService ssoService) {
        this.hangarConfig = hangarConfig;
        this.userDao = userDao;
        this.sessionsDao = sessionsDao;
        this.apiKeyDao = apiKeyDao;
        this.authenticationManager = authenticationManager;
        this.roleService = roleService;
        this.permissionService = permissionService;
        this.ssoService = ssoService;
    }

    public boolean apiAction(Permission perms, String apiScope, String...args) {
        AuthCredentials credentials = parseAuthHeader();
        if (credentials.session == null) {
            throw unAuth("No session specified");
        }
        String token = credentials.session;
        ApiAuthInfo apiKey = apiKeyDao.get().getApiAuthInfo(token);
        if (apiKey == null) {
            throw unAuth("Invalid Session");
        }
        if (apiKey.getExpires().isBefore(OffsetDateTime.now())) {
            sessionsDao.get().delete(token);
            throw unAuth("Api session expired");
        }
        switch (apiScope.toLowerCase()) {
            case "global":
                if (!apiKey.getGlobalPerms().has(perms)) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN);
                }
                return true;
            case "project":
                if (args == null || args.length != 1) {
                    throw new IllegalStateException("Must have passed the pluginId to apiAction");
                }
                if (!permissionService.getProjectPermissions(apiKey.getUser().getId(), args[0]).has(perms)) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN);
                }
                return true;
            case "org":
                if (args == null || args.length != 1) {
                    throw new IllegalStateException("Must have passed the org name to apiAction");
                }
                if (!permissionService.getOrganizationPermissions(apiKey.getUser().getId(), args[0]).has(perms)) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN);
                }
                return true;
            default:
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    public ApiSessionResponse authenticateDev() {
        if (hangarConfig.fakeUser.isEnabled()) {
            hangarConfig.checkDebug();
            OffsetDateTime sessionExpiration = expiration(hangarConfig.api.session.getExpiration(), null);
            String uuidToken = UUID.randomUUID().toString();

            ApiSessionsTable apiSession = new ApiSessionsTable(uuidToken, null, hangarConfig.fakeUser.getId(), sessionExpiration);
            saveSession(apiSession);

            return new ApiSessionResponse(apiSession.getToken(), apiSession.getExpires(), SessionType.DEV);
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

    public ApiSessionResponse authenticatePublic(@Nullable SessionProperties properties) {
        OffsetDateTime sessionExpiration = expiration(hangarConfig.api.session.getExpiration(), properties != null ? properties.getExpiresIn() : null);
        String uuidToken = UUID.randomUUID().toString();

        if (sessionExpiration == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The requested expiration can't be used");
        }

        ApiSessionsTable apiSession = new ApiSessionsTable(uuidToken, null, null, sessionExpiration);
        saveSession(apiSession);

        return new ApiSessionResponse(apiSession.getToken(), apiSession.getExpires(), SessionType.PUBLIC);
    }

    public ApiSessionResponse authenticateKeyPublic(SessionProperties properties, long userId) {
        OffsetDateTime sessionExpiration = expiration(hangarConfig.api.session.getExpiration(), properties.getExpiresIn());
        OffsetDateTime publicSessionExpiration = expiration(hangarConfig.api.session.getPublicExpiration(), properties.getExpiresIn());
        String uuidToken = UUID.randomUUID().toString();

        AuthCredentials credentials = parseAuthHeader();
        SessionType sessionType;
        ApiSessionsTable apiSession;
        if (credentials.apiKey != null) {
            if (!API_KEY_PATTERN.matcher(credentials.apiKey).find()) {
                throw unAuth("No valid apikey parameter found in Authorization");
            }

            if (sessionExpiration == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The requested expiration can't be used");
            }
            // I THINK that is how its setup, couldn't really figure it out via ore
            String identifier = credentials.apiKey.split("\\.")[0];
            String token = credentials.apiKey.split("\\.")[1];
            ApiKeysTable apiKeysTable = apiKeyDao.get().findApiKey(identifier, token);
            if (apiKeysTable == null) {
                throw unAuth("Invalid api key");
            }
            apiSession = new ApiSessionsTable(uuidToken, apiKeysTable.getId(), apiKeysTable.getOwnerId(), sessionExpiration);
            sessionType = SessionType.KEY;
        } else {
            if (publicSessionExpiration == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The requested expiration can't be used");
            }
            apiSession = new ApiSessionsTable(uuidToken, null, null, publicSessionExpiration);
            sessionType = SessionType.PUBLIC;
        }

        sessionsDao.get().insert(apiSession);

        return new ApiSessionResponse(apiSession.getToken(), apiSession.getExpires(), sessionType);
    }

    public ApiSessionResponse authenticateUser(long userId) {
        OffsetDateTime sessionExpiration = expiration( hangarConfig.api.session.getExpiration(), null);
        String uuidToken = UUID.randomUUID().toString();

        ApiSessionsTable apiSession = new ApiSessionsTable(uuidToken, null, userId, sessionExpiration);
        saveSession(apiSession);

        return new ApiSessionResponse(apiSession.getToken(), apiSession.getExpires(), SessionType.USER);
    }

    private ApiSessionsTable saveSession(ApiSessionsTable apiSession) {
        return sessionsDao.get().insert(apiSession);
    }

    public void loginAsFakeUser() {
        String username = hangarConfig.fakeUser.getUsername();
        UsersTable userEntry = userDao.get().getByName(username);
        if (userEntry == null) {
            userEntry = new UsersTable();
            userEntry.setEmail(hangarConfig.fakeUser.getEmail());
            userEntry.setFullName(hangarConfig.fakeUser.getName());
            userEntry.setName(hangarConfig.fakeUser.getUsername());
            userEntry.setId(hangarConfig.fakeUser.getId());
            userEntry.setReadPrompts(new int[0]);

            userEntry = userDao.get().insert(userEntry);

            roleService.addGlobalRole(userEntry.getId(), Role.HANGAR_ADMIN.getRoleId());
        }
        authenticate(userEntry);
    }

    public boolean loginWithSSO(String sso, String sig) {
        Map<String, String> ssoData = ssoService.decode(sso, sig);
        String nonce = ssoData.get("nonce");
        String id = ssoData.get("external_id");
        if (nonce != null && id != null) {
            String returnUrl = ssoService.getReturnUrl(nonce);
            if (returnUrl != null) {
                UsersTable user = userDao.get().getById(Integer.parseInt(id));
                authenticate(user);
                return true;
            }
        }
        return false;
    }

    private void authenticate(UsersTable user) {
        // TODO properly do auth, remember me shit too
        Authentication auth = new HangarAuthentication(user.getName());
        authenticationManager.authenticate(auth);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private AuthCredentials parseAuthHeader() {
        String authHeader = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest().getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || authHeader.isBlank() || !authHeader.startsWith("HangarApi")) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        return AuthCredentials.parseHeader(authHeader);
    }

    private OffsetDateTime expiration(Duration expirationDuration, Long userChoice) {
        long durationSeconds = expirationDuration.toSeconds();

        if (userChoice == null) {
            return OffsetDateTime.now().plusSeconds(durationSeconds);
        } else if (userChoice <= durationSeconds) {
            return OffsetDateTime.now().plusSeconds(userChoice);
        } else {
            return null;
        }
    }

    private static class AuthCredentials {
        private final String apiKey;
        private final String session;

        private AuthCredentials(String apiKey, String session) {
            this.apiKey = apiKey;
            this.session = session;
        }

        public static AuthCredentials parseHeader(String authHeader) {
            Matcher apiKeyMatcher = API_KEY_HEADER_PATTERN.matcher(authHeader);
            Matcher sessionMatcher = SESSION_HEADER_PATTERN.matcher(authHeader);
            String apiKey = null;
            String sessionKey = null;
            if (apiKeyMatcher.find()) {
                apiKey = apiKeyMatcher.group();
            } else if (sessionMatcher.find()) {
                sessionKey = sessionMatcher.group();
            } else {
                System.out.println(apiKeyMatcher);
                System.out.println(sessionMatcher);
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Authorization header format");
            }
            return new AuthCredentials(apiKey, sessionKey);
        }
    }
}
