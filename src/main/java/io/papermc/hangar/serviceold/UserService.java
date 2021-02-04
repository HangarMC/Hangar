package io.papermc.hangar.serviceold;

import io.papermc.hangar.config.hangar.HangarConfig;
import io.papermc.hangar.db.customtypes.LoggedActionType;
import io.papermc.hangar.db.customtypes.LoggedActionType.UserContext;
import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.daoold.NotificationsDao;
import io.papermc.hangar.db.daoold.OrganizationDao;
import io.papermc.hangar.db.daoold.ProjectDao;
import io.papermc.hangar.db.daoold.UserDao;
import io.papermc.hangar.db.modelold.OrganizationsTable;
import io.papermc.hangar.db.modelold.UserOrganizationRolesTable;
import io.papermc.hangar.db.modelold.UsersTable;
import io.papermc.hangar.model.Permission;
import io.papermc.hangar.modelold.Prompt;
import io.papermc.hangar.modelold.Role;
import io.papermc.hangar.modelold.viewhelpers.FlagActivity;
import io.papermc.hangar.modelold.viewhelpers.HeaderData;
import io.papermc.hangar.modelold.viewhelpers.OrganizationData;
import io.papermc.hangar.modelold.viewhelpers.ReviewActivity;
import io.papermc.hangar.modelold.viewhelpers.UserData;
import io.papermc.hangar.modelold.viewhelpers.UserRole;
import io.papermc.hangar.security.HangarAuthenticationToken;
import io.papermc.hangar.service.PermissionService;
import io.papermc.hangar.util.RequestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

@Service("oldUserService")
public class UserService extends HangarService {

    private final HangarDao<UserDao> userDao;
    private final HangarDao<OrganizationDao> orgDao;
    private final HangarDao<ProjectDao> projectDao;
    private final HangarDao<OrganizationDao> organizationDao;
    private final HangarDao<NotificationsDao> notificationsDao;
    private final RoleService roleService;
    private final PermissionService permissionService;
    private final OrgService orgService;
    private final UserActionLogService userActionLogService;
    private final HangarConfig config;

    private final HttpServletRequest request;

    @Autowired
    public UserService(HangarDao<UserDao> userDao, HangarConfig config, HangarDao<OrganizationDao> orgDao, HangarDao<ProjectDao> projectDao, HangarDao<OrganizationDao> organizationDao, HangarDao<NotificationsDao> notificationsDao, RoleService roleService, PermissionService permissionService, OrgService orgService, UserActionLogService userActionLogService, HttpServletRequest request) {
        this.userDao = userDao;
        this.config = config;
        this.orgDao = orgDao;
        this.projectDao = projectDao;
        this.organizationDao = organizationDao;
        this.notificationsDao = notificationsDao;
        this.roleService = roleService;
        this.permissionService = permissionService;
        this.orgService = orgService;
        this.userActionLogService = userActionLogService;
        this.request = request;
    }

    @Bean
    @RequestScope
    public Supplier<Optional<UsersTable>> currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && !(authentication instanceof AnonymousAuthenticationToken)) {
            HangarAuthenticationToken auth = (HangarAuthenticationToken) authentication;
            return () -> Optional.ofNullable(userDao.get().getById(auth.getUserId()));
        }
        return Optional::empty;
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
