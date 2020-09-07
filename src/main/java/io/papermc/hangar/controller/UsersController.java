package io.papermc.hangar.controller;

import io.papermc.hangar.config.hangar.HangarConfig;
import io.papermc.hangar.db.customtypes.LoggedActionType;
import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.UserDao;
import io.papermc.hangar.db.model.NotificationsTable;
import io.papermc.hangar.db.model.OrganizationsTable;
import io.papermc.hangar.db.model.UsersTable;
import io.papermc.hangar.model.InviteFilter;
import io.papermc.hangar.model.NamedPermission;
import io.papermc.hangar.model.NotificationFilter;
import io.papermc.hangar.model.Prompt;
import io.papermc.hangar.model.viewhelpers.InviteSubject;
import io.papermc.hangar.model.viewhelpers.UserData;
import io.papermc.hangar.model.viewhelpers.UserRole;
import io.papermc.hangar.security.annotations.GlobalPermission;
import io.papermc.hangar.service.ApiKeyService;
import io.papermc.hangar.service.AuthenticationService;
import io.papermc.hangar.service.NotificationService;
import io.papermc.hangar.service.OrgService;
import io.papermc.hangar.service.PermissionService;
import io.papermc.hangar.service.RoleService;
import io.papermc.hangar.service.SitemapService;
import io.papermc.hangar.service.SsoService;
import io.papermc.hangar.service.SsoService.SignatureException;
import io.papermc.hangar.service.UserActionLogService;
import io.papermc.hangar.service.UserService;
import io.papermc.hangar.service.sso.AuthUser;
import io.papermc.hangar.service.sso.UrlWithNonce;
import io.papermc.hangar.util.AlertUtil;
import io.papermc.hangar.util.HangarException;
import io.papermc.hangar.util.Routes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.Optional;

@Controller
public class UsersController extends HangarController {

    private final HangarConfig hangarConfig;
    private final AuthenticationService authenticationService;
    private final UserService userService;
    private final OrgService orgService;
    private final RoleService roleService;
    private final ApiKeyService apiKeyService;
    private final PermissionService permissionService;
    private final NotificationService notificationService;
    private final SsoService ssoService;
    private final UserActionLogService userActionLogService;
    private final HangarDao<UserDao> userDao;
    private final SitemapService sitemapService;

    private final HttpServletRequest request;
    private final HttpServletResponse response;


    @Autowired
    public UsersController(HangarConfig hangarConfig, AuthenticationService authenticationService, UserService userService, OrgService orgService, RoleService roleService, ApiKeyService apiKeyService, PermissionService permissionService, NotificationService notificationService, SsoService ssoService, UserActionLogService userActionLogService, HangarDao<UserDao> userDao, SitemapService sitemapService, HttpServletRequest request, HttpServletResponse response) {
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
    }

    @GetMapping("/authors")
    public ModelAndView showAuthors(@RequestParam(required = false, defaultValue = "projects") String sort, @RequestParam(required = false, defaultValue = "1") int page) {
        ModelAndView mav = new ModelAndView("users/authors");
        mav.addObject("authors", userService.getAuthors(page, sort));
        mav.addObject("ordering", sort);
        mav.addObject("page", page);
        mav.addObject("pageSize", hangarConfig.user.getAuthorPageSize());
        return fillModel(mav);
    }

    @GetMapping("/login")
    public ModelAndView login(@RequestParam(defaultValue = "") String sso, @RequestParam(defaultValue = "") String sig, @RequestParam(defaultValue = "") String returnUrl, @CookieValue(value = "url", required = false) String redirectUrl, RedirectAttributes attributes) {
        if (hangarConfig.fakeUser.isEnabled()) {
            hangarConfig.checkDebug();

            authenticationService.loginAsFakeUser();

            return new ModelAndView("redirect:" + returnUrl);
        } else if (sso.isEmpty()) {
            String returnPath = returnUrl.isBlank() ? request.getRequestURI() : returnUrl;
            try {
                response.addCookie(new Cookie("url", returnPath));
                return redirectToSso(ssoService.getLoginUrl(hangarConfig.getBaseUrl() + "/login"), attributes);
            } catch (HangarException e) {
                AlertUtil.showAlert(attributes, AlertUtil.AlertType.ERROR, e.getMessageKey(), e.getArgs());
                return Routes.SHOW_HOME.getRedirect();
            }

        } else {
            AuthUser authUser = ssoService.authenticate(sso, sig);
            if (authUser == null) {
                AlertUtil.showAlert(attributes, AlertUtil.AlertType.ERROR, "error.loginFailed");
                return Routes.SHOW_HOME.getRedirect();
            }

            UsersTable user = userService.getOrCreate(authUser.getUsername(), authUser);
            roleService.removeAllGlobalRoles(user.getId());
            authUser.getGlobalRoles().forEach(role -> roleService.addGlobalRole(user.getId(), role.getRoleId()));
            authenticationService.setAuthenticatedUser(user);

            String redirectPath = redirectUrl != null ? redirectUrl : Routes.getRouteUrlOf("showHome");
            return new ModelAndView("redirect:" + redirectPath);
        }
    }

    private ModelAndView redirectToSso(UrlWithNonce urlWithNonce, RedirectAttributes attributes) {
        if (!hangarConfig.sso.isEnabled()) {
            AlertUtil.showAlert(attributes, AlertUtil.AlertType.ERROR, "error.noLogin");
            return Routes.SHOW_HOME.getRedirect();
        }
        ssoService.insert(urlWithNonce.getNonce());
        return new ModelAndView("redirect:" + urlWithNonce.getUrl());
    }


    @GetMapping("/logout")
    public ModelAndView logout(HttpSession session) {
        // TODO flash
        session.invalidate();
        return new ModelAndView("redirect:" + hangarConfig.getAuthUrl() + "/accounts/logout/");
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

    @GetMapping("/signup")
    public ModelAndView signUp(@RequestParam(defaultValue = "") String returnUrl, RedirectAttributes attributes) {
        try {
            return redirectToSso(ssoService.getSignupUrl(returnUrl), attributes);
        } catch (HangarException e) {
            AlertUtil.showAlert(attributes, AlertUtil.AlertType.ERROR, e.getMessageKey(), e.getArgs());
            return Routes.SHOW_HOME.getRedirect();
        }
    }

    @GetMapping("/staff")
    public Object showStaff(@RequestParam(required = false, defaultValue = "roles") String sort, @RequestParam(required = false, defaultValue = "1") int page) {
        ModelAndView mav = new ModelAndView("users/staff");
        mav.addObject("staff", userService.getStaff(page, sort));
        mav.addObject("ordering", sort);
        mav.addObject("page", page);
        mav.addObject("pageSize", hangarConfig.user.getAuthorPageSize());
        return fillModel(mav);
    }

    @PostMapping("/verify")
    public ModelAndView verify(@RequestParam String returnPath, RedirectAttributes attributes) {
        try {
            return redirectToSso(ssoService.getVerifyUrl(hangarConfig.getBaseUrl() + returnPath), attributes);
        } catch (HangarException e) {
            AlertUtil.showAlert(attributes, AlertUtil.AlertType.ERROR, e.getMessageKey(), e.getArgs());
            return Routes.SHOW_HOME.getRedirect();
        }
    }

    @GetMapping("/{user}")
    public ModelAndView showProjects(@PathVariable String user) {
        ModelAndView mav = new ModelAndView("users/projects");
        OrganizationsTable organizationsTable = orgService.getOrganization(user);
        mav.addObject("u", userService.getUserData(user));
        if (organizationsTable != null) {
            mav.addObject("o", orgService.getOrganizationData(organizationsTable, null));
            mav.addObject("so", orgService.getScopedOrganizationData(organizationsTable));
        }
        return fillModel(mav);
    }

    @Secured("ROLE_USER")
    @GetMapping("/{user}/settings/apiKeys")
    public ModelAndView editApiKeys(@PathVariable String user) {
        ModelAndView mav = new ModelAndView("users/apiKeys");
        UserData userData = userService.getUserData(user);
        long userId = userData.getUser().getId();
        mav.addObject("u", userData);
        mav.addObject("keys", apiKeyService.getKeys(userId));
        mav.addObject("perms", permissionService.getPossibleOrganizationPermissions(userId).add(permissionService.getPossibleProjectPermissions(userId)).add(userData.getUserPerm()).toNamed());
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

    @Secured("ROLE_USER")
    @PostMapping(value = "/{user}/settings/tagline")
    public ModelAndView saveTagline(@PathVariable String user, @RequestParam("tagline") String tagline) {
        if (tagline.length() > hangarConfig.user.getMaxTaglineLen()) {
            ModelAndView mav = showProjects(user);
            AlertUtil.showAlert(mav, AlertUtil.AlertType.ERROR, "error.tagline.tooLong", String.valueOf(hangarConfig.user.getMaxTaglineLen()));
            return Routes.USERS_SHOW_PROJECTS.getRedirect(user);
        }
        UsersTable usersTable = userDao.get().getByName(user);
        userActionLogService.user(request, LoggedActionType.USER_TAGLINE_CHANGED.with(LoggedActionType.UserContext.of(usersTable.getId())), tagline, Optional.ofNullable(usersTable.getTagline()).orElse(""));
        usersTable.setTagline(tagline);
        userDao.get().update(usersTable);
        return Routes.USERS_SHOW_PROJECTS.getRedirect(user);
    }

    @GetMapping(value = "/{user}/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public String userSitemap(@PathVariable String user) {
        UsersTable usersTable = userDao.get().getByName(user);
        if (usersTable == null) {
            response.setStatus(404);
            return null;
        }
        return sitemapService.getUserSitemap(usersTable);
    }

}

