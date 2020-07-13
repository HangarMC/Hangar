package me.minidigger.hangar.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import me.minidigger.hangar.controller.HangarController;

@Controller
public class OrgController extends HangarController {

    @RequestMapping("/organisations/invite/{id}/{status}")
    public Object setInviteStatus(@PathVariable Object id, @PathVariable Object status) {
        return null; // TODO implement setInviteStatus request controller
    }

    @GetMapping("/organisations/new")
    public Object showCreator() {
        return null; // TODO implement showCreator request controller
    }

    @PostMapping("/organisations/new")
    public Object create() {
        return null; // TODO implement create request controller
    }

    @RequestMapping("/organisations/{organisations}/settings/avatar")
    public Object updateAvatar(@PathVariable Object organisations) {
        return null; // TODO implement updateAvatar request controller
    }

    @RequestMapping("/organisations/{organisations}/settings/members")
    public Object updateMembers(@PathVariable Object organisations) {
        return null; // TODO implement updateMembers request controller
    }

    @RequestMapping("/organisations/{organisations}/settings/members/remove")
    public Object removeMember(@PathVariable Object organisations) {
        return null; // TODO implement removeMember request controller
    }

}

