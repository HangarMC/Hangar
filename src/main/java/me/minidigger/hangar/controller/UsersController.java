package me.minidigger.hangar.controller;
import me.minidigger.hangar.model.viewhelpers.UserData;
import me.minidigger.hangar.service.*;
import me.minidigger.hangar.db.customtypes.LoggedActionType;
import me.minidigger.hangar.db.customtypes.LoggedActionType.UserContext;
import me.minidigger.hangar.util.AlertUtil;
import me.minidigger.hangar.util.AlertUtil.AlertType;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import me.minidigger.hangar.config.HangarConfig;
import me.minidigger.hangar.db.dao.HangarDao;
import me.minidigger.hangar.db.dao.UserDao;
import me.minidigger.hangar.db.model.UsersTable;
import me.minidigger.hangar.util.RouteHelper;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Controller
public class UsersController extends HangarController {

    private final HangarDao<UserDao> userDao;
    private final HangarConfig hangarConfig;
    private final AuthenticationService authenticationService;
    private final UserService userService;
    private final UserActionLogService userActionLogService;
    private final RouteHelper routeHelper;
    private final ApiKeyService apiKeyService;
    private final PermissionService permissionService;
    private final SsoService ssoService;

    @Autowired
    public UsersController(HangarConfig hangarConfig, HangarDao<UserDao> userDao, AuthenticationService authenticationService, ApplicationController applicationController, UserService userService, UserActionLogService userActionLogService, RouteHelper routeHelper, ApiKeyService apiKeyService, PermissionService permissionService, SsoService ssoService) {
        this.hangarConfig = hangarConfig;
        this.userDao = userDao;
        this.authenticationService = authenticationService;
        this.userService = userService;
        this.userActionLogService = userActionLogService;
        this.routeHelper = routeHelper;
        this.apiKeyService = apiKeyService;
        this.permissionService = permissionService;
        this.ssoService = ssoService;
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
    public ModelAndView login(@RequestParam(defaultValue = "") String sso, @RequestParam(defaultValue = "") String sig, @RequestParam(defaultValue = "") String returnUrl) {
        if (hangarConfig.fakeUser.isEnabled()) {
            hangarConfig.checkDebug();

            authenticationService.loginAsFakeUser();

            return new ModelAndView("redirect:" + returnUrl);
        } else if (sso.isEmpty() || sig.isBlank()) {
            // redirect to SSO
            String nonce = ssoService.setReturnUrl(returnUrl);
            String authRedirectUrl = ServletUriComponentsBuilder.fromCurrentRequestUri()
                    .replaceQuery("")
                    .build().toString() + "&nonce=" + nonce; // spongeauth doesn't like properly-formatted query strings
            Pair<String, String> ssoData = ssoService.encode(Map.of("return_sso_url", authRedirectUrl));
            String ssoUrl = UriComponentsBuilder.fromHttpUrl(hangarConfig.getAuthUrl() + hangarConfig.sso.getLoginUrl())
                    .queryParam("sso", ssoData.getLeft())
                    .queryParam("sig", ssoData.getRight())
                    .build().toString();
            return new ModelAndView("redirect:" + ssoUrl);
        } else {
            boolean success = authenticationService.loginWithSSO(sso, sig);
            if (success) {
                String nonce = ssoService.decode(sso, sig).get("nonce");
                returnUrl = ssoService.getReturnUrl(nonce);
                ssoService.clearReturnUrl(nonce);
                return new ModelAndView("redirect:" + returnUrl);
            } else {
                return new ModelAndView("redirect:" + routeHelper.getRouteUrl("showHome"));
            }
        }
    }

    @RequestMapping("/logout")
    public RedirectView logout(HttpSession session) {
        session.invalidate();
        return new RedirectView(routeHelper.getRouteUrl("showHome")); // TODO redirect to sso
    }

    @Secured("ROLE_USER")
    @RequestMapping("/notifications")
    public Object showNotifications(@RequestParam Object notificationFilter, @RequestParam Object inviteFilter) {
        return null; // TODO implement showNotifications request controller
    }

    @Secured("ROLE_USER")
    @RequestMapping("/notifications/read/{id}")
    public Object markNotificationRead(@PathVariable Object id) {
        return null; // TODO implement markNotificationRead request controller
    }

    @Secured("ROLE_USER")
    @RequestMapping("/prompts/read/{id}")
    public Object markPromptRead(@PathVariable Object id) {
        return null; // TODO implement markPromptRead request controller
    }

    @RequestMapping("/signup")
    public Object signUp() {
        return null; // TODO implement signUp request controller
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
        userActionLogService.user(request, LoggedActionType.USER_TAGLINE_CHANGED.with(UserContext.of(usersTable.getId())), tagline, usersTable.getTagline());
        usersTable.setTagline(tagline);
        userDao.get().update(usersTable);
        return new ModelAndView("redirect:" + routeHelper.getRouteUrl("users.showProjects", user));
    }

    @RequestMapping("/{user}/sitemap.xml")
    public Object userSitemap(@PathVariable Object user) {
        return null; // TODO implement userSitemap request controller
    }

}

