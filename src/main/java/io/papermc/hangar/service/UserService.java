package io.papermc.hangar.service;

import io.papermc.hangar.config.CacheConfig;
import io.papermc.hangar.config.hangar.HangarConfig;
import io.papermc.hangar.controller.LoginController;
import io.papermc.hangar.db.customtypes.LoggedActionType;
import io.papermc.hangar.db.customtypes.LoggedActionType.UserContext;
import io.papermc.hangar.db.daoold.HangarDao;
import io.papermc.hangar.db.daoold.NotificationsDao;
import io.papermc.hangar.db.daoold.OrganizationDao;
import io.papermc.hangar.db.daoold.ProjectDao;
import io.papermc.hangar.db.daoold.UserDao;
import io.papermc.hangar.db.modelold.OrganizationsTable;
import io.papermc.hangar.db.modelold.UserOrganizationRolesTable;
import io.papermc.hangar.db.modelold.UserSessionsTable;
import io.papermc.hangar.db.modelold.UsersTable;
import io.papermc.hangar.model.Permission;
import io.papermc.hangar.modelold.Prompt;
import io.papermc.hangar.modelold.Role;
import io.papermc.hangar.modelold.SsoSyncData;
import io.papermc.hangar.modelold.UserOrdering;
import io.papermc.hangar.modelold.viewhelpers.Author;
import io.papermc.hangar.modelold.viewhelpers.FlagActivity;
import io.papermc.hangar.modelold.viewhelpers.HeaderData;
import io.papermc.hangar.modelold.viewhelpers.OrganizationData;
import io.papermc.hangar.modelold.viewhelpers.ReviewActivity;
import io.papermc.hangar.modelold.viewhelpers.Staff;
import io.papermc.hangar.modelold.viewhelpers.UserData;
import io.papermc.hangar.modelold.viewhelpers.UserRole;
import io.papermc.hangar.security.HangarAuthentication;
import io.papermc.hangar.service.sso.AuthUser;
import io.papermc.hangar.util.RequestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

@Service
public class UserService extends HangarService {

    private final HangarDao<UserDao> userDao;
    private final HangarDao<OrganizationDao> orgDao;
    private final HangarDao<ProjectDao> projectDao;
    private final HangarDao<OrganizationDao> organizationDao;
    private final HangarDao<NotificationsDao> notificationsDao;
    private final RoleService roleService;
    private final PermissionService permissionService;
    private final OrgService orgService;
    private final SessionService sessionService;
    private final UserActionLogService userActionLogService;
    private final HangarConfig config;

    private final HttpServletRequest request;

    @Autowired
    public UserService(HangarDao<UserDao> userDao, HangarConfig config, HangarDao<OrganizationDao> orgDao, HangarDao<ProjectDao> projectDao, HangarDao<OrganizationDao> organizationDao, HangarDao<NotificationsDao> notificationsDao, RoleService roleService, PermissionService permissionService, OrgService orgService, SessionService sessionService, UserActionLogService userActionLogService, HttpServletRequest request) {
        this.userDao = userDao;
        this.config = config;
        this.orgDao = orgDao;
        this.projectDao = projectDao;
        this.organizationDao = organizationDao;
        this.notificationsDao = notificationsDao;
        this.roleService = roleService;
        this.permissionService = permissionService;
        this.orgService = orgService;
        this.sessionService = sessionService;
        this.userActionLogService = userActionLogService;
        this.request = request;
    }

    @Bean
    @RequestScope
    public Supplier<Optional<UsersTable>> currentUser() {
        if (request.getRequestURI().startsWith("/api/old/v1")) { // TODO remove once all of the old v1 is gone
            Cookie sessionCookie = WebUtils.getCookie(request, LoginController.AUTH_TOKEN_NAME);
            if (sessionCookie == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
            } else {
                UserSessionsTable session = sessionService.getSession(sessionCookie.getValue());
                if (session.hasExpired()) {
                    sessionService.deleteSession(session);
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
                }
                Optional<UsersTable> usersTableOptional = Optional.ofNullable(userDao.get().getById(session.getUserId()));
                return () -> usersTableOptional;
            }
        } else {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && !(authentication instanceof AnonymousAuthenticationToken)) {
                HangarAuthentication auth = (HangarAuthentication) authentication;
                return () -> Optional.ofNullable(userDao.get().getById(auth.getUserId()));
            }
            return Optional::empty;
        }

    }

    @Bean
    @RequestScope
    public Supplier<UsersTable> usersTable() {
        Map<String, String> pathParams = RequestUtil.getPathParams(request);
        if (pathParams.containsKey("user")) {
            UsersTable user = userDao.get().getByName(pathParams.get("user"));
            if (user == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            }
            return () -> user;
        } else {
            return () -> null;
        }
    }

    @Bean
    @RequestScope
    public Supplier<UserData> userData() {
        //noinspection SpringConfigurationProxyMethods
        return () -> this.getUserData(usersTable().get());
    }

    public HeaderData getHeaderData() {
        if (currentUser.get().isEmpty()) {
            return HeaderData.UNAUTHENTICATED;
        }
        UsersTable curUser = currentUser.get().get();
        Permission perms = permissionService.getGlobalPermissions(curUser.getId());

        boolean hasUnreadNotifs = notificationsDao.get().hasUnreadNotifications(curUser.getId());
        boolean hasUnresolvedFlags = perms.has(Permission.ModNotesAndFlags) && notificationsDao.get().hasUnresolvedFlags();
        boolean hasProjectApprovals = perms.has(Permission.ModNotesAndFlags.add(Permission.SeeHidden)) && notificationsDao.get().hasProjectApprovals(curUser.getId());
        boolean hasReviewQueue = perms.has(Permission.Reviewer) && notificationsDao.get().hasReviewQueue();
        return new HeaderData(
                curUser,
                permissionService.getGlobalPermissions(curUser.getId()),
                hasUnreadNotifs || hasUnresolvedFlags || hasProjectApprovals || hasReviewQueue,
                hasUnreadNotifs,
                hasUnresolvedFlags,
                hasProjectApprovals,
                hasReviewQueue
        );
    }

    @CacheEvict(value = CacheConfig.AUTHORS_CACHE,  allEntries = true)
    public void clearAuthorsCache() {}

    @Cacheable(CacheConfig.AUTHORS_CACHE)
    public List<Author> getAuthors(int page, String sort) {
        boolean reverse = false;
        if (sort.startsWith("-")) {
            sort = sort.substring(1);
            reverse = true;
        }

        long pageSize = config.user.getAuthorPageSize();
        long offset = (page - 1) * pageSize;

        return userDao.get().getAuthors(offset, pageSize, userOrder(reverse, sort));
    }

    @CacheEvict(value = CacheConfig.STAFF_CACHE,  allEntries = true)
    public void clearStaffCache() {}

    @Cacheable(CacheConfig.STAFF_CACHE)
    public List<Staff> getStaff(int page, String sort) {
        boolean reverse = false;
        if (sort.startsWith("-")) {
            sort = sort.substring(1);
            reverse = true;
        }

        long pageSize = config.user.getAuthorPageSize();
        long offset = (page - 1) * pageSize;

        return userDao.get().getStaff(offset, pageSize, userOrder(reverse, sort));
    }

    private String userOrder(boolean reverse, String sortStr) {
        String sort = reverse ? " ASC" : " DESC";

        String sortUserName = "sq.name" + sort;
        String thenSortUserName = "," + sortUserName;

        switch (sortStr) {
            case UserOrdering.JoinDate:
                return "ORDER BY sq.join_date" + sort;
            case UserOrdering.UserName:
                return "ORDER BY " + sortUserName;
            case UserOrdering.Projects:
                return "ORDER BY sq.count" + sort + thenSortUserName;
            case UserOrdering.Role:
                return "ORDER BY sq.permission::BIGINT" + sort + " NULLS LAST" + ", sq.role" + sort + thenSortUserName;
            default:
                return " ";
        }
    }

    public void setLocked(UsersTable user, boolean locked) {
        user.setIsLocked(locked);
        if (locked) {
            userActionLogService.user(request, LoggedActionType.USER_LOCKED.with(UserContext.of(user.getId())), user.getName() + " is now locked", user.getName() + " was unlocked");
        } else {
            userActionLogService.user(request, LoggedActionType.USER_UNLOCKED.with(UserContext.of(user.getId())), user.getName() + " is now unlocked", user.getName() + " was locked");
        }
        userDao.get().update(user);
    }

    public UserData getUserData(long userId) {
        return getUserData(userDao.get().getById(userId));
    }

    public UserData getUserData(String userName) {
        return getUserData(userDao.get().getByName(userName));
    }

    public UserData getUserData(UsersTable user) {
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        // TODO getUserData
        int projectCount = projectDao.get().getProjectCountByUserId(user.getId());
        Map<OrganizationsTable, UserOrganizationRolesTable> dbOrgs = orgDao.get().getUserOrganizationsAndRoles(user.getId());
        Map<OrganizationData, UserRole<UserOrganizationRolesTable>> organizations = new HashMap<>();
        dbOrgs.forEach((organization, userOrganizationRolesTable) -> {
            organizations.put(orgService.getOrganizationData(organization, null), new UserRole<>(userOrganizationRolesTable));
        });
        List<Role> globalRoles = roleService.getGlobalRolesForUser(user.getId(), null);
        boolean isOrga = globalRoles.contains(Role.ORGANIZATION);
        Permission userPerm = permissionService.getGlobalPermissions(user.getId());
        Permission orgaPerm = Permission.None; // TODO perms here
        return new UserData(getHeaderData(), user, isOrga, projectCount, organizations, globalRoles, userPerm, orgaPerm);
    }

    public UsersTable getUsersTable(long userId) {
        return userDao.get().getById(userId);
    }

    public List<UsersTable> getUsers(List<String> userNames) {
        return userDao.get().getUsers(userNames);
    }

    public UsersTable getOrCreate(String username, AuthUser authUser) {
        UsersTable user = userDao.get().getByName(username);
        if (user == null) {
            user = new UsersTable(
                    authUser.getId(),
                    null,
                    authUser.getUsername(),
                    authUser.getEmail(),
                    null,
                    List.of(),
                    false,
                    authUser.getLang().toLanguageTag()
            );
            userDao.get().insert(user);
        }
        return user;
    }

    public void ssoSyncUser(SsoSyncData syncData) {
        UsersTable user = userDao.get().getById(syncData.getExternalId());
        if (user == null) {
            user = new UsersTable(
                    syncData.getExternalId(),
                    syncData.getFullName(),
                    syncData.getUsername(),
                    syncData.getEmail(),
                    null,
                    List.of(),
                    false,
                    null
            );
            user = userDao.get().insert(user);
        } else {
            user.setFullName(syncData.getFullName());
            user.setName(syncData.getUsername());
            user.setEmail(syncData.getEmail());
            userDao.get().update(user);
        }

        for (String roleName : syncData.getAddGroups()) {
            Role role = Role.fromValue(roleName);
            if (role != null) {
                roleService.addGlobalRole(user.getId(), role.getRoleId());
            }
        }

        for (String roleName : syncData.getRemoveGroups()) {
            Role role = Role.fromValue(roleName);
            if (role != null) {
                roleService.removeGlobalRole(user.getId(), role.getRoleId());
            }
        }
    }

    public List<ReviewActivity> getReviewActivity(String username) {
        return userDao.get().getReviewActivity(username);
    }

    public List<FlagActivity> getFlagActivity(String username) {
        return userDao.get().getFlagActivity(username);
    }

    public void markPromptAsRead(Prompt prompt) {
        Optional<UsersTable> currUser = currentUser.get();
        if (currUser.isPresent()) {
            if (!currUser.get().getReadPrompts().contains(prompt.ordinal())) {
                currUser.get().getReadPrompts().add(prompt.ordinal());
            }
            userDao.get().update(currUser.get());
        }
    }

    public List<OrganizationsTable> getOrganizationsUserCanUploadTo(UsersTable usersTable) {
        return organizationDao.get().getOrgsUserCanUploadTo(usersTable.getId());
    }
}
