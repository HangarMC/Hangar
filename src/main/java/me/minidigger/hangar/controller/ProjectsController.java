package me.minidigger.hangar.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import me.minidigger.hangar.db.dao.HangarDao;
import me.minidigger.hangar.db.dao.UserDao;
import me.minidigger.hangar.db.model.ProjectPagesTable;
import me.minidigger.hangar.db.model.ProjectsTable;
import me.minidigger.hangar.db.model.UsersTable;
import me.minidigger.hangar.model.Category;
import me.minidigger.hangar.model.Permission;
import me.minidigger.hangar.model.viewhelpers.ProjectData;
import me.minidigger.hangar.model.viewhelpers.ScopedProjectData;
import me.minidigger.hangar.service.OrgService;
import me.minidigger.hangar.service.UserService;
import me.minidigger.hangar.service.project.ProjectFactory;
import me.minidigger.hangar.service.project.ProjectService;
import me.minidigger.hangar.util.AlertUtil;
import me.minidigger.hangar.util.HangarException;
import me.minidigger.hangar.util.RouteHelper;

@Controller
public class ProjectsController extends HangarController {

    public static final Pattern ID_PATTERN = Pattern.compile("[a-z][a-z0-9-_]{0,63}");

    private final UserService userService;
    private final OrgService orgService;
    private final RouteHelper routeHelper;
    private final ProjectFactory projectFactory;
    private final HangarDao<UserDao> userDao;
    private final ProjectService projectService;

    @Autowired
    public ProjectsController(UserService userService, OrgService orgService, RouteHelper routeHelper, ProjectFactory projectFactory, HangarDao<UserDao> userDao, ProjectService projectService) {
        this.userService = userService;
        this.orgService = orgService;
        this.routeHelper = routeHelper;
        this.projectFactory = projectFactory;
        this.userDao = userDao;
        this.projectService = projectService;
    }

    @PostMapping(value = "/new", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public Object createProject(@RequestParam("name") String name,
                                @RequestParam("pluginId") String pluginId,
                                @RequestParam("category") String cat,
                                @RequestParam("description") String description,
                                @RequestParam("owner") long owner) {
        UsersTable currentUser = userService.getCurrentUser();
        // check if creation should be prevented
        String uploadError = projectFactory.getUploadError(currentUser);
        if (uploadError != null) {
            ModelAndView mav = showCreator();
            AlertUtil.showAlert(mav, "error", uploadError);
            return fillModel(mav);
        }
        // validate input
        Category category = Category.fromTitle(cat);
        if (category == null) {
            ModelAndView mav = showCreator();
            AlertUtil.showAlert(mav, "error", "error.project.categoryNotFound");
            return fillModel(mav);
        }
        if (!ID_PATTERN.matcher(pluginId).matches()) {
            ModelAndView mav = showCreator();
            AlertUtil.showAlert(mav, "error", "error.project.invalidPluginId");
            return fillModel(mav);
        }
        // TODO check that currentUser can upload to owner
        // find owner
        UsersTable ownerUser = userDao.get().getById(owner);
        if (ownerUser == null) {
            ModelAndView mav = showCreator();
            AlertUtil.showAlert(mav, "error", "error.project.ownerNotFound");
            return fillModel(mav);
        }

        // create project
        ProjectsTable project;
        try {
            project = projectFactory.createProject(ownerUser, name, pluginId, category, description);
        } catch (HangarException ex) {
            ModelAndView mav = showCreator();
            AlertUtil.showAlert(mav, "error", ex.getMessageKey());
            return fillModel(mav);
        }

        // refresh home page
        // service
        //        .runDbCon(SharedQueries.refreshHomeView.run)
        //        .runAsync(TaskUtils.logCallback("Failed to refresh home page", logger))
        //        .to[F]

        return new ModelAndView("redirect:" + routeHelper.getRouteUrl("projects.show", project.getOwnerName(), project.getSlug()));
    }

    @RequestMapping("/invite/{id}/{status}")
    public Object setInviteStatus(@PathVariable Object id, @PathVariable Object status) {
        return null; // TODO implement setInviteStatus request controller
    }

    @RequestMapping("/invite/{id}/{status}/{behalf}")
    public Object setInviteStatusOnBehalf(@PathVariable Object id, @PathVariable Object status, @PathVariable Object behalf) {
        return null; // TODO implement setInviteStatusOnBehalf request controller
    }

    @GetMapping("/new")
    public ModelAndView showCreator() {
        ModelAndView mav = new ModelAndView("projects/create");
        mav.addObject("createProjectOrgas", orgService.getOrgsWithPerm(userService.getCurrentUser(), Permission.CreateProject));
        return fillModel(mav);
    }

    @GetMapping("/{author}/{slug}")
    public ModelAndView show(@PathVariable String author, @PathVariable String slug) {
        ModelAndView mav = new ModelAndView("projects/pages/view");
        ProjectData projectData = projectService.getProjectData(author, slug);
        mav.addObject("p", projectData);
        ScopedProjectData sp = new ScopedProjectData();
        sp.setPermissions(Permission.IsProjectOwner.add(Permission.EditPage));
        mav.addObject("sp", sp);
        mav.addObject("rootPages", new HashMap<ProjectPagesTable, List<ProjectPagesTable>>());
        mav.addObject("page", projectService.getPage(projectData.getProject().getId(), "Home"));
        mav.addObject("parentPage");
        mav.addObject("pageCount", 0);
        mav.addObject("editorOpen", false);
        // TODO implement show request controller
        return fillModel(mav);
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

