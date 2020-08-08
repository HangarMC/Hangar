package me.minidigger.hangar.controller;

import me.minidigger.hangar.service.RoleService;
import me.minidigger.hangar.service.sso.AuthUser;
import me.minidigger.hangar.service.sso.UrlWithNonce;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Map;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import me.minidigger.hangar.config.hangar.HangarConfig;
import me.minidigger.hangar.db.customtypes.LoggedActionType;
import me.minidigger.hangar.db.dao.HangarDao;
import me.minidigger.hangar.db.dao.UserDao;
import me.minidigger.hangar.db.model.NotificationsTable;
import me.minidigger.hangar.db.model.UsersTable;
import me.minidigger.hangar.model.InviteFilter;
import me.minidigger.hangar.model.NotificationFilter;
import me.minidigger.hangar.model.viewhelpers.InviteSubject;
import me.minidigger.hangar.model.viewhelpers.UserData;
import me.minidigger.hangar.model.viewhelpers.UserRole;
import me.minidigger.hangar.service.ApiKeyService;
import me.minidigger.hangar.service.AuthenticationService;
import me.minidigger.hangar.service.NotificationService;
import me.minidigger.hangar.service.PermissionService;
import me.minidigger.hangar.service.SsoService;
import me.minidigger.hangar.service.UserActionLogService;
import me.minidigger.hangar.service.UserService;
import me.minidigger.hangar.util.AlertUtil;
import me.minidigger.hangar.util.AlertUtil.AlertType;
import me.minidigger.hangar.util.RouteHelper;

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

    public UsersController(HangarConfig hangarConfig, RouteHelper routeHelper, AuthenticationService authenticationService, UserService userService, RoleService roleService, ApiKeyService apiKeyService, PermissionService permissionService, NotificationService notificationService, SsoService ssoService, UserActionLogService userActionLogService, HangarDao<UserDao> userDao) {
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
    public ModelAndView login(@RequestParam(defaultValue = "") String sso, @RequestParam(defaultValue = "") String sig, @RequestParam(defaultValue = "") String returnUrl, RedirectAttributes attributes) {
        if (hangarConfig.fakeUser.isEnabled()) {
            hangarConfig.checkDebug();

            authenticationService.loginAsFakeUser();

            return new ModelAndView("redirect:" + returnUrl);
        } else if (sso.isEmpty()) {
            return redirectToSso(ssoService.getLoginUrl(hangarConfig.getBaseUrl() + "/login"), attributes);
        } else {
            AuthUser authUser = ssoService.authenticate(sso, sig);
            if (authUser == null) {
                AlertUtil.showAlert(attributes, AlertType.ERROR, "error.loginFailed");
                return new ModelAndView("redirect:" + routeHelper.getRouteUrl("showHome"));
            }

            UsersTable user = userService.getOrCreate(authUser.getUsername(), authUser);
            roleService.removeAllGlobalRoles(user.getId());
            authUser.getGlobalRoles().forEach(role -> {
                roleService.addGlobalRole(user.getId(), role.getRoleId());
            });
            authenticationService.authenticate(user);

            // TODO redirect to flash
            return new ModelAndView("redirect:" + routeHelper.getRouteUrl("showHome"));

        }

//        else if (sso.isEmpty() || sig.isBlank()) {
//            // redirect to SSO
//            String authRedirectUrl = ssoService.generateAuthReturnUrl(returnUrl);
//            Pair<String, String> ssoData = ssoService.encode(Map.of("return_sso_url", authRedirectUrl));
//            String ssoUrl = ssoService.getAuthLoginUrl(ssoData.getLeft(), ssoData.getRight());
//            return new ModelAndView("redirect:" + ssoUrl);
//        } else {
//            boolean success = authenticationService.loginWithSSO(sso, sig);
//            if (success) {
//                String nonce = ssoService.decode(sso, sig).get("nonce");
//                returnUrl = ssoService.getReturnUrl(nonce);
//                ssoService.clearReturnUrl(nonce);
//                return new ModelAndView("redirect:" + returnUrl);
//            } else {
//                return new ModelAndView("redirect:" + routeHelper.getRouteUrl("showHome"));
//            }
//        }
    }

    private ModelAndView redirectToSso(UrlWithNonce urlWithNonce, RedirectAttributes attributes) {
        if (!hangarConfig.sso.isEnabled()) {
            AlertUtil.showAlert(attributes, AlertType.ERROR, "error.noLogin");
            return new ModelAndView("redirect:" + routeHelper.getRouteUrl("showHome"));
        }
        ssoService.insert(urlWithNonce.getNonce());
        return new ModelAndView("redirect:" + urlWithNonce.getUrl());
    }


    @RequestMapping("/logout")
    public ModelAndView logout(HttpSession session) {
        session.invalidate();
        return new ModelAndView("redirect:" + ssoService.getAuthLogoutUrl());
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
    public ModelAndView signUp(@RequestParam(defaultValue = "") String returnUrl) {
        // redirect to SSO
        String authRedirectUrl = ssoService.generateAuthReturnUrl(returnUrl);
        Pair<String, String> ssoData = ssoService.encode(Map.of("return_sso_url", authRedirectUrl));
        String ssoUrl = ssoService.getAuthSignupUrl(ssoData.getLeft(), ssoData.getRight());
        return new ModelAndView("redirect:" + ssoUrl);
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
    public Object verify(@RequestParam Object returnPath) {
        return null; // TODO implement verify request controller
    }

    @RequestMapping("/{user}")
    public ModelAndView showProjects(@PathVariable String user) {
        ModelAndView mav = new ModelAndView("users/projects");
        mav.addObject("u", userService.getUserData(user));
        mav.addObject("o", null);
        return fillModel(mav);
    }

    @Secured("ROLE_USER")
    @GetMapping("/{user}/settings/apiKeys")
    public ModelAndView editApiKeys(@PathVariable String user) {
        ModelAndView mav = new ModelAndView("users/apiKeys");
        UserData userData = userService.getUserData(user);
        long userId = userData.getUser().getId();
        mav.addObject("u", userData);
        mav.addObject("keys", apiKeyService.getKeysForUser(userId));
        mav.addObject("perms", permissionService.getPossibleOrganizationPermissions(userId).add(permissionService.getPossibleProjectPermissions(userId)).add(userData.getUserPerm()).toNamed());
        return fillModel(mav); // TODO implement editApiKeys request controller
    }

    @Secured("ROLE_USER")
    @RequestMapping("/{user}/settings/lock/{locked}")
    public RedirectView setLocked(@PathVariable String user, @PathVariable boolean locked, @RequestParam Object sso, @RequestParam Object sig) {
        // TODO auth
        userService.setLocked(user, locked);
        return new RedirectView(routeHelper.getRouteUrl("users.showProjects", user));
    }

    @Secured("ROLE_USER")
    @PostMapping(value = "/{user}/settings/tagline")
    public ModelAndView saveTagline(@PathVariable String user, @RequestParam("tagline") String tagline, HttpServletRequest request) {
        if (tagline.length() > hangarConfig.user.getMaxTaglineLen()) {
            ModelAndView mav = showProjects(user);
            AlertUtil.showAlert(mav, AlertType.ERROR, "error.tagline.tooLong"); // TODO pass length param to key
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

