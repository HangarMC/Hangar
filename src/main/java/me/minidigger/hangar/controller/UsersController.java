package me.minidigger.hangar.controller;

import me.minidigger.hangar.util.AlertUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;

import me.minidigger.hangar.config.HangarConfig;
import me.minidigger.hangar.db.dao.HangarDao;
import me.minidigger.hangar.db.dao.UserDao;
import me.minidigger.hangar.db.model.UsersTable;
import me.minidigger.hangar.service.AuthenticationService;
import me.minidigger.hangar.service.UserService;

@Controller
public class UsersController extends HangarController {

    private final HangarDao<UserDao> userDao;
    private final HangarConfig hangarConfig;
    private final AuthenticationService authenticationService;
    private final ApplicationController applicationController;
    private final UserService userService;

    @Autowired
    public UsersController(HangarConfig hangarConfig, HangarDao<UserDao> userDao, AuthenticationService authenticationService, ApplicationController applicationController, UserService userService) {
        this.hangarConfig = hangarConfig;
        this.userDao = userDao;
        this.authenticationService = authenticationService;
        this.applicationController = applicationController;
        this.userService = userService;
    }

    @RequestMapping("/authors")
    public ModelAndView showAuthors(@RequestParam(required = false, defaultValue = "projects") String sort, @RequestParam(required = false, defaultValue = "1") int page) {
        ModelAndView mav = new ModelAndView("users/authors");
        mav.addObject("authors", userService.getAuthors(page, sort));
        mav.addObject("ordering", sort);
        mav.addObject("page", page);
        mav.addObject("pageSize", hangarConfig.getAuthorPageSize());
        return fillModel(mav);
    }

    @RequestMapping("/login")
    public ModelAndView login(@RequestParam(defaultValue = "") String sso, @RequestParam(defaultValue = "") String sig, @RequestParam String returnUrl) {
        if (hangarConfig.isFakeUserEnabled()) {
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
                return applicationController.showHome(); // on a scale of banana to kneny, how bad is it to call another controller?
            }
        }
    }

    @RequestMapping("/logout")
    public ModelAndView logout(HttpSession session) {
        session.invalidate();
        return new ModelAndView("redirect:/"); // TODO redirect to sso
    }

    @RequestMapping("/notifications")
    public Object showNotifications(@RequestParam Object notificationFilter, @RequestParam Object inviteFilter) {
        return null; // TODO implement showNotifications request controller
    }

    @RequestMapping("/notifications/read/{id}")
    public Object markNotificationRead(@PathVariable Object id) {
        return null; // TODO implement markNotificationRead request controller
    }

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
        mav.addObject("pageSize", hangarConfig.getAuthorPageSize());
        return fillModel(mav);
    }

    @RequestMapping("/verify")
    public Object verify(@RequestParam Object returnPath) {
        return null; // TODO implement verify request controller
    }

    @RequestMapping("/{user}")
    public ModelAndView showProjects(@PathVariable String user) {
        UsersTable dbUser = userDao.get().getByName(user);
        return showProjects(dbUser);
    }

    private ModelAndView showProjects(UsersTable dbUser) {
        if (dbUser == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        ModelAndView mav = new ModelAndView("users/projects");
        mav.addObject("u", userService.getUserData(dbUser));
        mav.addObject("o", null);
        return fillModel(mav);
    }

    @RequestMapping("/{user}/settings/apiKeys")
    public Object editApiKeys(@PathVariable Object user) {
        return null; // TODO implement editApiKeys request controller
    }

    @RequestMapping("/{user}/settings/lock/{locked}")
    public Object setLocked(@PathVariable Object user, @PathVariable Object locked, @RequestParam Object sso, @RequestParam Object sig) {
        return null; // TODO implement setLocked request controller
    }

    @RequestMapping(value = "/{user}/settings/tagline", method = RequestMethod.POST)
    public ModelAndView saveTagline(@PathVariable String user, @RequestParam("tagline") String tagline) {
        if (tagline.length() > hangarConfig.getMaxTaglineLen()) {
            ModelAndView mav = showProjects(user);
            AlertUtil.showAlert(mav, AlertUtil.ERROR, "error.tagline.tooLong"); // TODO pass length param to key
            return showProjects(user);
        }
        // TODO user action log
        UsersTable usersTable = userDao.get().getByName(user);
        usersTable.setTagline(tagline);
        usersTable = userDao.get().update(usersTable);
        return showProjects(usersTable);
    }

    @RequestMapping("/{user}/sitemap.xml")
    public Object userSitemap(@PathVariable Object user) {
        return null; // TODO implement userSitemap request controller
    }

}

