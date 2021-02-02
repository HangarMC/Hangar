package io.papermc.hangar.controllerold;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.papermc.hangar.config.hangar.HangarConfig;
import io.papermc.hangar.db.customtypes.LoggedActionType;
import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.daoold.UserDao;
import io.papermc.hangar.db.modelold.NotificationsTable;
import io.papermc.hangar.db.modelold.UsersTable;
import io.papermc.hangar.model.internal.sso.AuthUser;
import io.papermc.hangar.modelold.InviteFilter;
import io.papermc.hangar.modelold.NamedPermission;
import io.papermc.hangar.modelold.NotificationFilter;
import io.papermc.hangar.modelold.Prompt;
import io.papermc.hangar.modelold.viewhelpers.InviteSubject;
import io.papermc.hangar.modelold.viewhelpers.OrganizationData;
import io.papermc.hangar.modelold.viewhelpers.ScopedOrganizationData;
import io.papermc.hangar.modelold.viewhelpers.UserData;
import io.papermc.hangar.modelold.viewhelpers.UserRole;
import io.papermc.hangar.securityold.annotations.GlobalPermission;
import io.papermc.hangar.service.PermissionService;
import io.papermc.hangar.service.internal.SSOService;
import io.papermc.hangar.serviceold.ApiKeyService;
import io.papermc.hangar.serviceold.AuthenticationService;
import io.papermc.hangar.serviceold.NotificationService;
import io.papermc.hangar.serviceold.OrgService;
import io.papermc.hangar.serviceold.RoleService;
import io.papermc.hangar.serviceold.SitemapService;
import io.papermc.hangar.serviceold.SsoService.SignatureException;
import io.papermc.hangar.serviceold.UserActionLogService;
import io.papermc.hangar.serviceold.UserService;
import io.papermc.hangar.util.AlertUtil;
import io.papermc.hangar.util.Routes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Controller("oldUsersController")
public class UsersController extends HangarController {

    private final ObjectMapper mapper;
    private final HangarConfig hangarConfig;
    private final AuthenticationService authenticationService;
    private final UserService userService;
    private final OrgService orgService;
    private final RoleService roleService;
    private final ApiKeyService apiKeyService;
    private final PermissionService permissionService;
    private final NotificationService notificationService;
    private final SSOService ssoService;
    private final UserActionLogService userActionLogService;
    private final HangarDao<UserDao> userDao;
    private final SitemapService sitemapService;

    private final HttpServletRequest request;
    private final HttpServletResponse response;
    private final Supplier<UsersTable> usersTable;


    @Autowired
    public UsersController(ObjectMapper mapper, HangarConfig hangarConfig, AuthenticationService authenticationService, UserService userService, OrgService orgService, RoleService roleService, ApiKeyService apiKeyService, PermissionService permissionService, NotificationService notificationService, SSOService ssoService, UserActionLogService userActionLogService, HangarDao<UserDao> userDao, SitemapService sitemapService, HttpServletRequest request, HttpServletResponse response, Supplier<UsersTable> usersTable) {
        this.mapper = mapper;
        this.hangarConfig = hangarConfig;
        this.authenticationService = authenticationService;
        this.userService = userService;
        this.orgService = orgService;
        this.roleService = roleService;
        this.apiKeyService = apiKeyService;
        this.permissionService = permissionService;
        this.notificationService = notificationService;
        this.ssoService = ssoService;
        this.userActionLogService = userActionLogService;
        this.userDao = userDao;
        this.sitemapService = sitemapService;
        this.request = request;
        this.response = response;
        this.usersTable = usersTable;
    }

    @Secured("ROLE_USER")
    @GetMapping("/notifications")
    public ModelAndView showNotifications(@RequestParam(defaultValue = "UNREAD") NotificationFilter notificationFilter, @RequestParam(defaultValue = "ALL") InviteFilter inviteFilter) {
        ModelAndView mav = new ModelAndView("users/notifications");
        mav.addObject("notificationFilter", notificationFilter);
        mav.addObject("inviteFilter", inviteFilter);
        Map<NotificationsTable, UsersTable> notifications = notificationService.getNotifications(notificationFilter);
        mav.addObject("notifications", notifications);
        Map<UserRole<?>, InviteSubject<?>> invites = notificationService.getInvites(inviteFilter);
        mav.addObject("invites", invites);
        return fillModel(mav);
    }

    @Secured("ROLE_USER")
    @PostMapping("/notifications/read/{id}")
    public ResponseEntity<String> markNotificationRead(@PathVariable long id) {
        if (notificationService.markAsRead(id)) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Secured("ROLE_USER")
    @PostMapping("/prompts/read/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void markPromptRead(@PathVariable("id") Prompt prompt) {
        if (prompt == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid prompt id");
        }
        userService.markPromptAsRead(prompt);
    }

    @GetMapping("/{user}")
    public ModelAndView showProjects(@PathVariable String user) {
        ModelAndView mav = new ModelAndView("users/projects");
        UserData userData = userService.getUserData(user);
        Optional<OrganizationData> orgData = Optional.ofNullable(orgService.getOrganizationData(userData.getUser()));
        Optional<ScopedOrganizationData> scopedOrgData = orgData.map(organizationData -> orgService.getScopedOrganizationData(organizationData.getOrg()));
        mav.addObject("u", userService.getUserData(user));
        mav.addObject("o", orgData);
        mav.addObject("so", scopedOrgData);
        return fillModel(mav);
    }

    @GlobalPermission(NamedPermission.EDIT_API_KEYS)
    @Secured("ROLE_USER")
    @GetMapping("/{user}/settings/apiKeys")
    public ModelAndView editApiKeys(@PathVariable String user) {
        ModelAndView mav = new ModelAndView("users/apiKeys");
        UserData userData = userService.getUserData(user);
        Optional<OrganizationData> orgData = Optional.ofNullable(orgService.getOrganizationData(userData.getUser()));
        Optional<ScopedOrganizationData> scopedOrgData = orgData.map(organizationData -> orgService.getScopedOrganizationData(organizationData.getOrg()));
        long userId = userData.getUser().getId();
        mav.addObject("u", userData);
        mav.addObject("o", orgData);
        mav.addObject("so", scopedOrgData);
        mav.addObject("keys", apiKeyService.getKeys(userId));
        List<NamedPermission> perms = permissionService.getPossibleOrganizationPermissions(userId).add(permissionService.getPossibleProjectPermissions(userId)).add(userData.getUserPerm()).toNamed();
        mav.addObject("perms", perms.stream().map(perm -> mapper.createObjectNode().put("value", perm.toString()).put("name", perm.getFrontendName())).collect(Collectors.toList()));
        return fillModel(mav);
    }

    @GlobalPermission(NamedPermission.EDIT_OWN_USER_SETTINGS)
    @Secured("ROLE_USER")
    @GetMapping("/{user}/settings/lock/{locked}")
    public ModelAndView setLocked(@PathVariable String user, @PathVariable boolean locked, @RequestParam(required = false) String sso, @RequestParam(required = false) String sig) {
        UsersTable curUser = getCurrentUser();
        if (!hangarConfig.fakeUser.isEnabled()) {
            try {
                AuthUser authUser = ssoService.authenticate(sso, sig);
                if (authUser == null || authUser.getId() != curUser.getId()) {
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
                }
            } catch (SignatureException e) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
            }
        }

        if (!locked) {
            // TODO email!
        }
        userService.setLocked(curUser, locked);
        return Routes.USERS_SHOW_PROJECTS.getRedirect(user);
    }

    @GlobalPermission(NamedPermission.EDIT_OWN_USER_SETTINGS)
    @Secured("ROLE_USER")
    @PostMapping(value = "/{user}/settings/tagline")
    public ModelAndView saveTagline(@PathVariable("user") String username, @RequestParam("tagline") String tagline) {
        if (tagline.length() > hangarConfig.user.getMaxTaglineLen()) {
            ModelAndView mav = showProjects(username);
            AlertUtil.showAlert(mav, AlertUtil.AlertType.ERROR, "error.tagline.tooLong", String.valueOf(hangarConfig.user.getMaxTaglineLen()));
            return Routes.USERS_SHOW_PROJECTS.getRedirect(username);
        }
        UsersTable user = usersTable.get();
        userActionLogService.user(request, LoggedActionType.USER_TAGLINE_CHANGED.with(LoggedActionType.UserContext.of(user.getId())), tagline, Optional.ofNullable(user.getTagline()).orElse(""));
        user.setTagline(tagline);
        userDao.get().update(user);
        return Routes.USERS_SHOW_PROJECTS.getRedirect(username);
    }

    @GetMapping(value = "/{user}/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public String userSitemap(@PathVariable("user") String username) {
        UsersTable user = usersTable.get();
        return sitemapService.getUserSitemap(user);
    }

}

