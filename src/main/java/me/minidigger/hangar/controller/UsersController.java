package me.minidigger.hangar.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;

import me.minidigger.hangar.config.HangarConfig;
import me.minidigger.hangar.db.dao.HangarDao;
import me.minidigger.hangar.db.dao.UserDao;
import me.minidigger.hangar.db.model.UsersTable;
import me.minidigger.hangar.service.AuthenticationService;

@Controller
public class UsersController extends HangarController {

    private final HangarDao<UserDao> userDao;
    private final HangarConfig hangarConfig;
    private final AuthenticationService authenticationService;
    private final ApplicationController applicationController;

    @Autowired
    public UsersController(HangarConfig hangarConfig, HangarDao<UserDao> userDao, AuthenticationService authenticationService, ApplicationController applicationController) {
        this.hangarConfig = hangarConfig;
        this.userDao = userDao;
        this.authenticationService = authenticationService;
        this.applicationController = applicationController;
    }

    @RequestMapping("/authors")
    public ModelAndView showAuthors(@RequestParam(required = false) Object sort, @RequestParam(required = false) Object page) {
        return fillModel(new ModelAndView("users/authors")); // TODO implement showAuthors request controller
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
    public Object showStaff(@RequestParam(required = false) Object sort, @RequestParam(required = false) Object page) {
        return fillModel(new ModelAndView("users/staff")); // TODO implement showStaff request controller
    }

    @RequestMapping("/verify")
    public Object verify(@RequestParam Object returnPath) {
        return null; // TODO implement verify request controller
    }

    @RequestMapping("/{user}")
    public Object showProjects(@PathVariable String user) {
        // TODO hacky test shit
        UsersTable dbUser = userDao.get().getByName(user);
        if (dbUser == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        System.out.println(dbUser);

        ModelAndView mav = new ModelAndView("users/projects");
        mav.addObject("u", dbUser); // TODO proper frontend model
        mav.addObject("o", null);
        return fillModel(mav); // TODO implement showProjects request controller
    }

    @RequestMapping("/{user}/settings/apiKeys")
    public Object editApiKeys(@PathVariable Object user) {
        return null; // TODO implement editApiKeys request controller
    }

    @RequestMapping("/{user}/settings/lock/{locked}")
    public Object setLocked(@PathVariable Object user, @PathVariable Object locked, @RequestParam Object sso, @RequestParam Object sig) {
        return null; // TODO implement setLocked request controller
    }

    @RequestMapping("/{user}/settings/tagline")
    public Object saveTagline(@PathVariable Object user) {
        return null; // TODO implement saveTagline request controller
    }

    @RequestMapping("/{user}/sitemap.xml")
    public Object userSitemap(@PathVariable Object user) {
        return null; // TODO implement userSitemap request controller
    }

}

