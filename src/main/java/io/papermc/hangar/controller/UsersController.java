package io.papermc.hangar.controller;

import io.papermc.hangar.config.hangar.HangarConfig;
import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.UserDao;
import io.papermc.hangar.db.model.NotificationsTable;
import io.papermc.hangar.db.model.UsersTable;
import io.papermc.hangar.model.InviteFilter;
import io.papermc.hangar.model.NotificationFilter;
import io.papermc.hangar.service.NotificationService;
import io.papermc.hangar.service.SsoService;
import io.papermc.hangar.service.UserService;
import io.papermc.hangar.service.sso.AuthUser;
import io.papermc.hangar.service.sso.UrlWithNonce;
import io.papermc.hangar.util.AlertUtil;
import io.papermc.hangar.db.customtypes.LoggedActionType;
import io.papermc.hangar.model.viewhelpers.InviteSubject;
import io.papermc.hangar.model.viewhelpers.UserData;
import io.papermc.hangar.model.viewhelpers.UserRole;
import io.papermc.hangar.service.ApiKeyService;
import io.papermc.hangar.service.AuthenticationService;
import io.papermc.hangar.service.PermissionService;
import io.papermc.hangar.service.RoleService;
import io.papermc.hangar.service.UserActionLogService;
import io.papermc.hangar.util.HangarException;
import io.papermc.hangar.util.RouteHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.Optional;

@Controller
public class UsersController extends HangarController {

    private final HangarConfig hangarConfig;
    private final RouteHelper routeHelper;
    private final AuthenticationService authenticationService;
    private final UserService userService;
    private final RoleService roleService;
    private final ApiKeyService apiKeyService;
    private final PermissionService permissionService;
    private final NotificationService notificationService;
    private final SsoService ssoService;
    private final UserActionLogService userActionLogService;
    private final HangarDao<UserDao> userDao;

    private final HttpServletRequest request;
    private final HttpServletResponse response;


    @Autowired
    public UsersController(HangarConfig hangarConfig, RouteHelper routeHelper, AuthenticationService authenticationService, UserService userService, RoleService roleService, ApiKeyService apiKeyService, PermissionService permissionService, NotificationService notificationService, SsoService ssoService, UserActionLogService userActionLogService, HangarDao<UserDao> userDao, HttpServletRequest request, HttpServletResponse response) {
        this.hangarConfig = hangarConfig;
        this.routeHelper = routeHelper;
        this.authenticationService = authenticationService;
        this.userService = userService;
        this.roleService = roleService;
        this.apiKeyService = apiKeyService;
        this.permissionService = permissionService;
        this.notificationService = notificationService;
        this.ssoService = ssoService;
        this.userActionLogService = userActionLogService;
        this.userDao = userDao;
        this.request = request;
        this.response = response;
    }

    @RequestMapping("/authors")
    public ModelAndView showAuthors(@RequestParam(required = false, defaultValue = "projects") String sort, @RequestParam(required = false, defaultValue = "1") int page) {
        ModelAndView mav = new ModelAndView("users/authors");
        mav.addObject("authors", userService.getAuthors(page, sort));
        mav.addObject("ordering", sort);
        mav.addObject("page", page);
        mav.addObject("pageSize", hangarConfig.user.getAuthorPageSize());
        return fillModel(mav);
    }

    @RequestMapping("/login")
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
                return new ModelAndView("redirect:" + routeHelper.getRouteUrl("showHome"));
            }

        } else {
            AuthUser authUser = ssoService.authenticate(sso, sig);
            if (authUser == null) {
                AlertUtil.showAlert(attributes, AlertUtil.AlertType.ERROR, "error.loginFailed");
                return new ModelAndView("redirect:" + routeHelper.getRouteUrl("showHome"));
            }

            UsersTable user = userService.getOrCreate(authUser.getUsername(), authUser);
            roleService.removeAllGlobalRoles(user.getId());
            authUser.getGlobalRoles().forEach(role -> roleService.addGlobalRole(user.getId(), role.getRoleId()));
            authenticationService.setAuthenticatedUser(user);

            String redirectPath = redirectUrl != null ? redirectUrl : routeHelper.getRouteUrl("showHome");
            return new ModelAndView("redirect:" + redirectPath);
        }
    }

    private ModelAndView redirectToSso(UrlWithNonce urlWithNonce, RedirectAttributes attributes) {
        if (!hangarConfig.sso.isEnabled()) {
            AlertUtil.showAlert(attributes, AlertUtil.AlertType.ERROR, "error.noLogin");
            return new ModelAndView("redirect:" + routeHelper.getRouteUrl("showHome"));
        }
        ssoService.insert(urlWithNonce.getNonce());
        return new ModelAndView("redirect:" + urlWithNonce.getUrl());
    }


    @RequestMapping("/logout")
    public ModelAndView logout(HttpSession session) {
        // TODO flash
        session.invalidate();
        return new ModelAndView("redirect:" + hangarConfig.security.api.getUrl() + "/accounts/logout/");
    }

    @Secured("ROLE_USER")
    @GetMapping("/notifications")
    public ModelAndView showNotifications(@RequestParam(defaultValue = "UNREAD") NotificationFilter notificationFilter, @RequestParam(defaultValue = "ALL") InviteFilter inviteFilter) {
        ModelAndView mav = new ModelAndView("users/notifications");
        mav.addObject("notificationFilter", notificationFilter);
        mav.addObject("inviteFilter", inviteFilter);
        Map<NotificationsTable, UserData> notifications = notificationService.getNotifications(notificationFilter);
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
    @RequestMapping("/prompts/read/{id}")
    public Object markPromptRead(@PathVariable Object id) {
        return null; // TODO implement markPromptRead request controller
    }

    @RequestMapping("/signup")
    public ModelAndView signUp(@RequestParam(defaultValue = "") String returnUrl, RedirectAttributes attributes) {
        try {
            return redirectToSso(ssoService.getSignupUrl(returnUrl), attributes);
        } catch (HangarException e) {
            AlertUtil.showAlert(attributes, AlertUtil.AlertType.ERROR, e.getMessageKey(), e.getArgs());
            return new ModelAndView("redirect:" + routeHelper.getRouteUrl("showHome"));
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

    @RequestMapping("/verify")
    public ModelAndView verify(@RequestParam String returnPath, RedirectAttributes attributes) {
        try {
            return redirectToSso(ssoService.getVerifyUrl(returnPath), attributes);
        } catch (HangarException e) {
            AlertUtil.showAlert(attributes, AlertUtil.AlertType.ERROR, e.getMessageKey(), e.getArgs());
            return new ModelAndView("redirect:" + routeHelper.getRouteUrl("showHome"));
        }
    }

    @RequestMapping("/{user}")
    public ModelAndView showProjects(@PathVariable String user) {
        ModelAndView mav = new ModelAndView("users/projects");
        mav.addObject("u", userService.getUserData(user));
        mav.addObject("o", null); // TODO organization
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

    @Secured("ROLE_USER")
    @RequestMapping("/{user}/settings/lock/{locked}")
    public RedirectView setLocked(@PathVariable String user, @PathVariable boolean locked, @RequestParam String sso, @RequestParam String sig) {
        // TODO auth
        userService.setLocked(user, locked);
        return new RedirectView(routeHelper.getRouteUrl("users.showProjects", user));
    }

    @Secured("ROLE_USER")
    @PostMapping(value = "/{user}/settings/tagline")
    public ModelAndView saveTagline(@PathVariable String user, @RequestParam("tagline") String tagline) {
        if (tagline.length() > hangarConfig.user.getMaxTaglineLen()) {
            ModelAndView mav = showProjects(user);
            AlertUtil.showAlert(mav, AlertUtil.AlertType.ERROR, "error.tagline.tooLong", String.valueOf(hangarConfig.user.getMaxTaglineLen()));
            return new ModelAndView("redirect:" + routeHelper.getRouteUrl("users.showProjects", user));
        }
        UsersTable usersTable = userDao.get().getByName(user);
        userActionLogService.user(request, LoggedActionType.USER_TAGLINE_CHANGED.with(LoggedActionType.UserContext.of(usersTable.getId())), tagline, Optional.ofNullable(usersTable.getTagline()).orElse(""));
        usersTable.setTagline(tagline);
        userDao.get().update(usersTable);
        return new ModelAndView("redirect:" + routeHelper.getRouteUrl("users.showProjects", user));
    }

    @RequestMapping("/{user}/sitemap.xml")
    public Object userSitemap(@PathVariable Object user) {
        return null; // TODO implement userSitemap request controller
    }

}

