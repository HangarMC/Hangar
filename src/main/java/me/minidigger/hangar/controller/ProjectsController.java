package me.minidigger.hangar.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import me.minidigger.hangar.model.Permission;
import me.minidigger.hangar.service.OrgService;
import me.minidigger.hangar.service.UserService;

@Controller
public class ProjectsController extends HangarController {

    private final UserService userService;
    private final OrgService orgService;

    public ProjectsController(UserService userService, OrgService orgService) {
        this.userService = userService;
        this.orgService = orgService;
    }

    @PostMapping("/")
    public Object createProject() {
        return null; // TODO implement createProject request controller
    }

    @RequestMapping("/invite/{id}/{status}")
    public Object setInviteStatus(@PathVariable Object id, @PathVariable Object status) {
        return null; // TODO implement setInviteStatus request controller
    }

    @RequestMapping("/invite/{id}/{status}/{behalf}")
    public Object setInviteStatusOnBehalf(@PathVariable Object id, @PathVariable Object status, @PathVariable Object behalf) {
        return null; // TODO implement setInviteStatusOnBehalf request controller
    }

    @RequestMapping("/new")
    public Object showCreator() {
        ModelAndView mav = new ModelAndView("projects/create");
        mav.addObject("createProjectOrgas", orgService.getOrgsWithPerm(userService.getCurrentUser(), Permission.CreateProject));
        return fillModel(mav);
    }

    @RequestMapping("/{author}/{slug}")
    public Object show(@PathVariable Object author, @PathVariable Object slug) {
        return null; // TODO implement show request controller
    }

    @RequestMapping("/{author}/{slug}/discuss")
    public Object showDiscussion(@PathVariable Object author, @PathVariable Object slug) {
        return null; // TODO implement showDiscussion request controller
    }

    @RequestMapping("/{author}/{slug}/discuss/reply")
    public Object postDiscussionReply(@PathVariable Object author, @PathVariable Object slug) {
        return null; // TODO implement postDiscussionReply request controller
    }

    @RequestMapping("/{author}/{slug}/flag")
    public Object flag(@PathVariable Object author, @PathVariable Object slug) {
        return null; // TODO implement flag request controller
    }

    @RequestMapping("/{author}/{slug}/flags")
    public Object showFlags(@PathVariable Object author, @PathVariable Object slug) {
        return null; // TODO implement showFlags request controller
    }

    @PostMapping("/{author}/{slug}/icon")
    public Object uploadIcon(@PathVariable Object author, @PathVariable Object slug) {
        return null; // TODO implement uploadIcon request controller
    }

    @GetMapping("/{author}/{slug}/icon")
    public Object showIcon(@PathVariable Object author, @PathVariable Object slug) {
        return null; // TODO implement showIcon request controller
    }

    @GetMapping("/{author}/{slug}/icon/pending")
    public Object showPendingIcon(@PathVariable Object author, @PathVariable Object slug) {
        return null; // TODO implement showPendingIcon request controller
    }

    @RequestMapping("/{author}/{slug}/icon/reset")
    public Object resetIcon(@PathVariable Object author, @PathVariable Object slug) {
        return null; // TODO implement resetIcon request controller
    }

    @RequestMapping("/{author}/{slug}/manage")
    public Object showSettings(@PathVariable Object author, @PathVariable Object slug) {
        return null; // TODO implement showSettings request controller
    }

    @RequestMapping("/{author}/{slug}/manage/delete")
    public Object softDelete(@PathVariable Object author, @PathVariable Object slug) {
        return null; // TODO implement softDelete request controller
    }

    @RequestMapping("/{author}/{slug}/manage/hardDelete")
    public Object delete(@PathVariable Object author, @PathVariable Object slug) {
        return null; // TODO implement delete request controller
    }

    @RequestMapping("/{author}/{slug}/manage/members/remove")
    public Object removeMember(@PathVariable Object author, @PathVariable Object slug) {
        return null; // TODO implement removeMember request controller
    }

    @RequestMapping("/{author}/{slug}/manage/rename")
    public Object rename(@PathVariable Object author, @PathVariable Object slug) {
        return null; // TODO implement rename request controller
    }

    @RequestMapping("/{author}/{slug}/manage/save")
    public Object save(@PathVariable Object author, @PathVariable Object slug) {
        return null; // TODO implement save request controller
    }

    @RequestMapping("/{author}/{slug}/manage/sendforapproval")
    public Object sendForApproval(@PathVariable Object author, @PathVariable Object slug) {
        return null; // TODO implement sendForApproval request controller
    }

    @RequestMapping("/{author}/{slug}/notes")
    public Object showNotes(@PathVariable Object author, @PathVariable Object slug) {
        return null; // TODO implement showNotes request controller
    }

    @RequestMapping("/{author}/{slug}/notes/addmessage")
    public Object addMessage(@PathVariable Object author, @PathVariable Object slug) {
        return null; // TODO implement addMessage request controller
    }

    @RequestMapping("/{author}/{slug}/stars")
    public Object showStargazers(@PathVariable Object author, @PathVariable Object slug, @RequestParam Object page) {
        return null; // TODO implement showStargazers request controller
    }

    @RequestMapping("/{author}/{slug}/stars/toggle")
    public Object toggleStarred(@PathVariable Object author, @PathVariable Object slug) {
        return null; // TODO implement toggleStarred request controller
    }

    @RequestMapping("/{author}/{slug}/visible/{visibility}")
    public Object setVisible(@PathVariable Object author, @PathVariable Object slug, @PathVariable Object visibility) {
        return null; // TODO implement setVisible request controller
    }

    @RequestMapping("/{author}/{slug}/watchers")
    public Object showWatchers(@PathVariable Object author, @PathVariable Object slug, @RequestParam Object page) {
        return null; // TODO implement showWatchers request controller
    }

    @RequestMapping("/{author}/{slug}/watchers/{watching}")
    public Object setWatching(@PathVariable Object author, @PathVariable Object slug, @PathVariable Object watching) {
        return null; // TODO implement setWatching request controller
    }

}

