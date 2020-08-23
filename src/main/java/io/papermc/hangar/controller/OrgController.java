package io.papermc.hangar.controller;

import io.papermc.hangar.config.hangar.HangarConfig;
import io.papermc.hangar.db.model.OrganizationsTable;
import io.papermc.hangar.model.Role;
import io.papermc.hangar.service.OrgFactory;
import io.papermc.hangar.service.OrgService;
import io.papermc.hangar.service.UserService;
import io.papermc.hangar.util.AlertUtil;
import io.papermc.hangar.util.AlertUtil.AlertType;
import io.papermc.hangar.util.HangarException;
import io.papermc.hangar.util.RouteHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public ModelAndView showCreator(RedirectAttributes attributes, ModelMap modelMap) {
        if (orgService.getUserOwnedOrgs(userService.getCurrentUser().getId()).size() >= hangarConfig.org.getCreateLimit()) {
            AlertUtil.showAlert(attributes, AlertUtil.AlertType.ERROR, "error.org.createLimit", String.valueOf(hangarConfig.org.getCreateLimit()));
            return new ModelAndView("redirect:" + routeHelper.getRouteUrl("showHome"));
        }
        return fillModel(AlertUtil.transferAlerts(new ModelAndView("createOrganization"), modelMap));
    }

    @Secured("ROLE_USER")
    @PostMapping(value = "/organisations/new", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ModelAndView create(@RequestParam String name, @RequestParam(required = false) List<Long> users, @RequestParam(required = false) List<Role> roles, RedirectAttributes attributes) {
        if (orgService.getUserOwnedOrgs(userService.getCurrentUser().getId()).size() >= hangarConfig.org.getCreateLimit()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "at create limit");
        }
        if (userService.getCurrentUser().isLocked()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        if (!hangarConfig.org.isEnabled()) {
            AlertUtil.showAlert(attributes, AlertType.ERROR, "error.org.disabled");
            return new ModelAndView("redirect:" + routeHelper.getRouteUrl("org.showCreator"));
        } else {
            Map<Long, Role> userRoles = new HashMap<>();
            if (users != null && roles != null) {
                if (users.size() != roles.size()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
                for (int i = 0; i < users.size(); i++) {
                    userRoles.put(users.get(i), roles.get(i));
                }
            }

            OrganizationsTable org;
            try {
                org = orgFactory.createOrganization(name, userService.getCurrentUser().getId(), userRoles);
            } catch (HangarException e) {
                AlertUtil.showAlert(attributes, AlertType.ERROR, e.getMessageKey(), e.getArgs());
                return new ModelAndView("redirect:" + routeHelper.getRouteUrl("org.showCreator"));
            }
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

