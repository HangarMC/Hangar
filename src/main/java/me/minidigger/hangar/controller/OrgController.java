package me.minidigger.hangar.controller;

import me.minidigger.hangar.config.HangarConfig;
import me.minidigger.hangar.db.model.OrganizationsTable;
import me.minidigger.hangar.service.OrgService;
import me.minidigger.hangar.service.OrgFactory;
import me.minidigger.hangar.service.UserService;
import me.minidigger.hangar.util.AlertUtil.AlertType;
import me.minidigger.hangar.util.RouteHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;

@Controller
public class OrgController extends HangarController {

    private final OrgService orgService;
    private final OrgFactory orgFactory;
    private final UserService userService;
    private final HangarConfig hangarConfig;
    private final RouteHelper routeHelper;

    @Autowired
    public OrgController(OrgService orgService, OrgFactory orgFactory, UserService userService, HangarConfig hangarConfig, RouteHelper routeHelper) {
        this.orgService = orgService;
        this.orgFactory = orgFactory;
        this.userService = userService;
        this.hangarConfig = hangarConfig;
        this.routeHelper = routeHelper;
    }

    @RequestMapping("/organisations/invite/{id}/{status}")
    public Object setInviteStatus(@PathVariable Object id, @PathVariable Object status) {
        return null; // TODO implement setInviteStatus request controller
    }

    @Secured("ROLE_USER")
    @GetMapping("/organisations/new")
    public ModelAndView showCreator(RedirectAttributes attributes, HttpServletRequest request) {
        if (orgService.getUserOwnedOrgs(userService.getCurrentUser().getId()).size() >= hangarConfig.org.getCreateLimit()) {
            attributes.addFlashAttribute("alertType", AlertType.ERROR.name().toUpperCase());
            attributes.addFlashAttribute("alertMsg", "error.org.createLimit"); // TODO arguments
            return new ModelAndView("redirect:" + routeHelper.getRouteUrl("showHome"));
        }
        return fillModel(new ModelAndView("createOrganization"));
    }

    @Secured("ROLE_USER")
    @PostMapping(value = "/organisations/new", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ModelAndView create(@RequestParam String name) { // TODO other params
        if (orgService.getUserOwnedOrgs(userService.getCurrentUser().getId()).size() >= hangarConfig.org.getCreateLimit()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "at create limit");
        }
        if (userService.getCurrentUser().isLocked()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        if (!hangarConfig.org.isEnabled()) {
            return new ModelAndView("redirect:" + routeHelper.getRouteUrl("org.showCreator"));
        } else {
            OrganizationsTable org = orgFactory.createOrganization(name, userService.getCurrentUser().getId(), new HashMap<>()); // TODO members
            return new ModelAndView("redirect:" + routeHelper.getRouteUrl("users.showProjects", org.getName()));
        }
    }

    @Secured("ROLE_USER")
    @RequestMapping("/organisations/{organisations}/settings/avatar")
    public Object updateAvatar(@PathVariable Object organisations) {
        return null; // TODO implement updateAvatar request controller
    }

    @Secured("ROLE_USER")
    @RequestMapping("/organisations/{organisations}/settings/members")
    public Object updateMembers(@PathVariable Object organisations) {
        return null; // TODO implement updateMembers request controller
    }

    @Secured("ROLE_USER")
    @RequestMapping("/organisations/{organisations}/settings/members/remove")
    public Object removeMember(@PathVariable Object organisations) {
        return null; // TODO implement removeMember request controller
    }

}

