package me.minidigger.hangar.controller;

import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class OrgController extends HangarController {

    @RequestMapping("/organisations/invite/{id}/{status}")
    public Object setInviteStatus(@PathVariable Object id, @PathVariable Object status) {
        return null; // TODO implement setInviteStatus request controller
    }

    @Secured("ROLE_USER")
    @GetMapping("/organisations/new")
    public ModelAndView showCreator() {
//        if (orgLimitReached) { TODO org limit
//            ModelAndView mav = new ModelAndView("forward:/");
//            AlertUtil.showAlert(mav, "error", "error.org.createLimit");
//            return fillModel(mav);
//        }
        return fillModel(new ModelAndView("createOrganization"));
    }

    @Secured("ROLE_USER")
    @PostMapping(value = "/organisations/new", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public Object create(@RequestBody MultiValueMap<String, String> body) {
        System.out.println(body);
        return null; // TODO implement create request controller
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

