package me.minidigger.hangar.controller;

import me.minidigger.hangar.db.customtypes.LoggedActionType;
import me.minidigger.hangar.db.customtypes.LoggedActionType.UserContext;
import me.minidigger.hangar.service.UserActionLogService;
import me.minidigger.hangar.util.AlertUtil;
import me.minidigger.hangar.util.AlertUtil.AlertType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import me.minidigger.hangar.config.HangarConfig;
import me.minidigger.hangar.db.dao.HangarDao;
import me.minidigger.hangar.db.dao.UserDao;
import me.minidigger.hangar.db.model.UsersTable;
import me.minidigger.hangar.service.AuthenticationService;
import me.minidigger.hangar.service.UserService;
import me.minidigger.hangar.util.RouteHelper;

@Controller
public class UsersController extends HangarController {

    private final HangarDao<UserDao> userDao;
    private final HangarConfig hangarConfig;
    private final AuthenticationService authenticationService;
    private final UserService userService;
    private final UserActionLogService userActionLogService;
    private final RouteHelper routeHelper;

    @Autowired
    public UsersController(HangarConfig hangarConfig, HangarDao<UserDao> userDao, AuthenticationService authenticationService, ApplicationController applicationController, UserService userService, UserActionLogService userActionLogService, RouteHelper routeHelper) {
        this.hangarConfig = hangarConfig;
        this.userDao = userDao;
        this.authenticationService = authenticationService;
        this.userService = userService;
        this.userActionLogService = userActionLogService;
        this.routeHelper = routeHelper;
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
    public ModelAndView login(@RequestParam(defaultValue = "") String sso, @RequestParam(defaultValue = "") String sig, @RequestParam String returnUrl) {
        if (hangarConfig.fakeUser.isEnabled()) {
            hangarConfig.checkDebug();

            authenticationService.loginAsFakeUser();

            return new ModelAndView("redirect:" + returnUrl);
        } else if (sso.isEmpty() || sig.isBlank()) {
            // TODO redirect to SSO
            return new ModelAndView("redirect:" + returnUrl);
        } else {
            // TODO decode sso, then login
            boolean success = authenticationService.loginWithSSO(sso, sig);
            if (success) {
                return new ModelAndView("redirect:" + returnUrl);
            } else {
                return new ModelAndView("redirect:" + routeHelper.getRouteUrl("showHome"));
            }
        }
    }

    @RequestMapping("/logout")
    public ModelAndView logout(HttpSession session) {
        session.invalidate();
        return new ModelAndView("redirect:/"); // TODO redirect to sso
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
        UsersTable dbUser = userDao.get().getByName(user);
        if (dbUser == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        ModelAndView mav = new ModelAndView("users/projects");
        mav.addObject("u", userService.getUserData(dbUser));
        mav.addObject("o", null);
        return fillModel(mav);
    }

    @Secured("ROLE_USER")
    @RequestMapping("/{user}/settings/apiKeys")
    public Object editApiKeys(@PathVariable Object user) {
        return null; // TODO implement editApiKeys request controller
    }

    @Secured("ROLE_USER")
    @RequestMapping("/{user}/settings/lock/{locked}")
    public Object setLocked(@PathVariable Object user, @PathVariable Object locked, @RequestParam Object sso, @RequestParam Object sig) {
        return null; // TODO implement setLocked request controller
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

