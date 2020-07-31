package me.minidigger.hangar.controller;


import me.minidigger.hangar.config.HangarConfig;
import me.minidigger.hangar.db.dao.HangarDao;
import me.minidigger.hangar.db.dao.ProjectDao;
import me.minidigger.hangar.db.dao.UserDao;
import me.minidigger.hangar.db.model.ProjectsTable;
import me.minidigger.hangar.db.model.UsersTable;
import me.minidigger.hangar.model.Category;
import me.minidigger.hangar.model.NamedPermission;
import me.minidigger.hangar.model.Permission;
import me.minidigger.hangar.model.Role;
import me.minidigger.hangar.model.viewhelpers.ProjectData;
import me.minidigger.hangar.model.viewhelpers.ProjectPage;
import me.minidigger.hangar.model.viewhelpers.ScopedProjectData;
import me.minidigger.hangar.security.annotations.GlobalPermission;
import me.minidigger.hangar.service.OrgService;
import me.minidigger.hangar.service.UserService;
import me.minidigger.hangar.service.project.PagesSerivce;
import me.minidigger.hangar.service.project.ProjectFactory;
import me.minidigger.hangar.service.project.ProjectService;
import me.minidigger.hangar.util.AlertUtil;
import me.minidigger.hangar.util.AlertUtil.AlertType;
import me.minidigger.hangar.util.HangarException;
import me.minidigger.hangar.util.RouteHelper;
import me.minidigger.hangar.util.StringUtils;
import me.minidigger.hangar.util.TriFunction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Collection;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;

@Controller
public class ProjectsController extends HangarController {

    public static final Pattern ID_PATTERN = Pattern.compile("[a-z][a-z0-9-_]{0,63}");

    private final HangarConfig hangarConfig;
    private final UserService userService;
    private final OrgService orgService;
    private final RouteHelper routeHelper;
    private final ProjectFactory projectFactory;
    private final HangarDao<UserDao> userDao;
    private final ProjectService projectService;
    private final PagesSerivce pagesSerivce;
    private final HangarDao<ProjectDao> projectDao;

    @Autowired
    public ProjectsController(HangarConfig hangarConfig, UserService userService, OrgService orgService, RouteHelper routeHelper, ProjectFactory projectFactory, HangarDao<UserDao> userDao, ProjectService projectService, HangarDao<ProjectDao> projectDao, PagesSerivce pagesSerivce) {
        this.hangarConfig = hangarConfig;
        this.userService = userService;
        this.orgService = orgService;
        this.routeHelper = routeHelper;
        this.projectFactory = projectFactory;
        this.userDao = userDao;
        this.projectService = projectService;
        this.pagesSerivce = pagesSerivce;
        this.projectDao = projectDao;
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
            AlertUtil.showAlert(mav, AlertType.ERROR, uploadError);
            return fillModel(mav);
        }
        // validate input
        Category category = Category.fromTitle(cat);
        if (category == null) {
            ModelAndView mav = showCreator();
            AlertUtil.showAlert(mav, AlertType.ERROR, "error.project.categoryNotFound");
            return fillModel(mav);
        }
        if (!ID_PATTERN.matcher(pluginId).matches()) {
            ModelAndView mav = showCreator();
            AlertUtil.showAlert(mav, AlertType.ERROR, "error.project.invalidPluginId");
            return fillModel(mav);
        }
        // TODO check that currentUser can upload to owner
        // find owner
        UsersTable ownerUser = userDao.get().getById(owner);
        if (ownerUser == null) {
            ModelAndView mav = showCreator();
            AlertUtil.showAlert(mav, AlertType.ERROR, "error.project.ownerNotFound");
            return fillModel(mav);
        }

        // create project
        ProjectsTable project;
        try {
            project = projectFactory.createProject(ownerUser, name, pluginId, category, description);
        } catch (HangarException ex) {
            ModelAndView mav = showCreator();
            AlertUtil.showAlert(mav, AlertType.ERROR, ex.getMessageKey());
            return fillModel(mav);
        }

        // refresh home page
        // service
        //        .runDbCon(SharedQueries.refreshHomeView.run)
        //        .runAsync(TaskUtils.logCallback("Failed to refresh home page", logger))
        //        .to[F]

        return new ModelAndView("redirect:" + routeHelper.getRouteUrl("projects.show", project.getOwnerName(), project.getSlug()));
    }

    @Secured("ROLE_USER")
    @RequestMapping("/invite/{id}/{status}")
    public Object setInviteStatus(@PathVariable Object id, @PathVariable Object status) {
        return null; // TODO implement setInviteStatus request controller
    }

    @Secured("ROLE_USER")
    @RequestMapping("/invite/{id}/{status}/{behalf}")
    public Object setInviteStatusOnBehalf(@PathVariable Object id, @PathVariable Object status, @PathVariable Object behalf) {
        return null; // TODO implement setInviteStatusOnBehalf request controller
    }

    @Secured("ROLE_USER")
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
        ScopedProjectData sp = projectService.getScopedProjectData(projectData.getProject().getId());
        mav.addObject("sp", sp);
        mav.addObject("page", ProjectPage.of(pagesSerivce.getPage(projectData.getProject().getId(), hangarConfig.pages.home.getName())));
        mav.addObject("parentPage");
        mav.addObject("editorOpen", false);
        pagesSerivce.fillPages(mav, projectData.getProject().getId());
        // TODO implement show request controller
        return fillModel(mav);
    }

    @RequestMapping("/{author}/{slug}/discuss")
    public Object showDiscussion(@PathVariable Object author, @PathVariable Object slug) {
        return null; // TODO implement showDiscussion request controller
    }

    @Secured("ROLE_USER")
    @RequestMapping("/{author}/{slug}/discuss/reply")
    public Object postDiscussionReply(@PathVariable Object author, @PathVariable Object slug) {
        return null; // TODO implement postDiscussionReply request controller
    }

    @Secured("ROLE_USER")
    @RequestMapping("/{author}/{slug}/flag")
    public Object flag(@PathVariable Object author, @PathVariable Object slug) {
        return null; // TODO implement flag request controller
    }

    @Secured("ROLE_USER")
    @RequestMapping("/{author}/{slug}/flags")
    public Object showFlags(@PathVariable Object author, @PathVariable Object slug) {
        return null; // TODO implement showFlags request controller
    }

    @Secured("ROLE_USER")
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

    @Secured("ROLE_USER")
    @RequestMapping("/{author}/{slug}/icon/reset")
    public Object resetIcon(@PathVariable Object author, @PathVariable Object slug) {
        return null; // TODO implement resetIcon request controller
    }

    @Secured("ROLE_USER")
    @RequestMapping("/{author}/{slug}/manage")
    public ModelAndView showSettings(@PathVariable String author, @PathVariable String slug) {
        ModelAndView mav = new ModelAndView("projects/settings");
        ProjectData projectData = projectService.getProjectData(author, slug);
        mav.addObject("p", projectData);
        ScopedProjectData scopedProjectData = projectService.getScopedProjectData(projectData.getProject().getId());
        mav.addObject("sp", scopedProjectData);
        // TODO add deploymentKey and iconUrl
        return fillModel(mav);
    }

    @Secured("ROLE_USER")
    @PostMapping(value = "/{author}/{slug}/manage/delete", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public RedirectView softDelete(@PathVariable String author, @PathVariable String slug, @RequestParam(required = false) String comment, RedirectAttributes ra) {
        ProjectData projectData = projectService.getProjectData(author, slug);
        projectFactory.softDeleteProject(projectData, comment);

        // TODO user action log
        ra.addFlashAttribute("alertType", AlertType.SUCCESS);
        ra.addFlashAttribute("alertMsg", "project.deleted");// TODO add old project name as msg arg
        return new RedirectView(routeHelper.getRouteUrl("showHome"));
    }

    @Secured("ROLE_USER")
    @RequestMapping("/{author}/{slug}/manage/hardDelete")
    public RedirectView delete(@PathVariable String author, @PathVariable String slug, RedirectAttributes ra) {
        ProjectData projectData = projectService.getProjectData(author, slug);
        projectFactory.hardDeleteProject(projectData);
        // TODO UAL
        ra.addFlashAttribute("alertType", AlertType.SUCCESS);
        ra.addFlashAttribute("alertMsg", "project.deleted");// TODO add old project name as msg arg
        return new RedirectView(routeHelper.getRouteUrl("showHome"));
    }

    @Secured("ROLE_USER")
    @RequestMapping("/{author}/{slug}/manage/members/remove")
    public Object removeMember(@PathVariable Object author, @PathVariable Object slug) {
        return null; // TODO implement removeMember request controller
    }

    @Secured("ROLE_USER")
    @PostMapping(value = "/{author}/{slug}/manage/rename", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public Object rename(@PathVariable String author, @PathVariable String slug, @RequestParam("name") String newName) {
        ProjectData projectData = projectService.getProjectData(author, slug);

        try {
            projectFactory.checkProjectAvailability(projectData.getProjectOwner(), newName);
        } catch (HangarException e) {
            ModelAndView mav = showSettings(author, slug);
            AlertUtil.showAlert(mav, AlertType.ERROR, "error.nameUnavailable");
            AlertUtil.showAlert(mav, AlertType.ERROR, e.getMessageKey());
            return mav;
        }

        projectData.getProject().setName(newName);
        projectData.getProject().setSlug(StringUtils.slugify(newName));
        projectDao.get().update(projectData.getProject());
        // TODO User action log
        return new RedirectView(routeHelper.getRouteUrl("projects.show", author, newName));
    }

    @Secured("ROLE_USER")
    @PostMapping(value = "/{author}/{slug}/manage/save", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public RedirectView save(@PathVariable String author,
                             @PathVariable String slug,
                             @RequestParam Category category,
                             @RequestParam(required = false) String keywords,
                             @RequestParam(required = false) String issues,
                             @RequestParam(required = false) String source,
                             @RequestParam(value = "license-name", required = false) String licenseName,
                             @RequestParam(value = "license-url", required = false) String licenseUrl,
                             @RequestParam("forum-sync") boolean forumSync,
                             @RequestParam String description,
                             @RequestParam("update-icon") boolean updateIcon) {
        ProjectsTable projectsTable = projectService.getProjectData(author, slug).getProject();
        projectsTable.setCategory(category);
        Set<String> keywordSet = keywords != null ? Set.of(keywords.split(" ")) : Set.of();
        projectsTable.setKeywords(keywordSet);
        projectsTable.setIssues(issues);
        projectsTable.setSource(source);
        projectsTable.setLicenseName(licenseName);
        projectsTable.setLicenseUrl(licenseUrl);
        projectsTable.setForumSync(forumSync);
        projectsTable.setDescription(description);
        projectDao.get().update(projectsTable);
        // TODO update icon handling
        // TODO user action log

        return new RedirectView(routeHelper.getRouteUrl("projects.show", author, slug)); // TODO implement save request controller
    }

    @Secured("ROLE_USER")
    @RequestMapping("/{author}/{slug}/manage/sendforapproval")
    public Object sendForApproval(@PathVariable Object author, @PathVariable Object slug) {
        return null; // TODO implement sendForApproval request controller
    }

    @Secured("ROLE_USER")
    @RequestMapping("/{author}/{slug}/notes")
    public Object showNotes(@PathVariable Object author, @PathVariable Object slug) {
        return null; // TODO implement showNotes request controller
    }

    @Secured("ROLE_USER")
    @RequestMapping("/{author}/{slug}/notes/addmessage")
    public Object addMessage(@PathVariable Object author, @PathVariable Object slug) {
        return null; // TODO implement addMessage request controller
    }

    @GetMapping("/{author}/{slug}/stars")
    public ModelAndView showStargazers(@PathVariable String author, @PathVariable String slug, @RequestParam(required = false, defaultValue = "1") Integer page) {
        return showUserGrid(author, slug, page, "Stargazers", projectService::getProjectStargazers);
    }

    @Secured("ROLE_USER")
    @PostMapping("/{author}/{slug}/stars/toggle")
    @ResponseStatus(HttpStatus.OK)
    public void toggleStarred(@PathVariable String author, @PathVariable String slug) {
        ProjectData projectData = projectService.getProjectData(author, slug);
        ScopedProjectData scopedProjectData = projectService.getScopedProjectData(projectData.getProject().getId());
        if (scopedProjectData.isStarred()) {
            userDao.get().removeStargazing(projectData.getProject().getId(), userService.getCurrentUser().getId());
        } else {
            userDao.get().setStargazing(projectData.getProject().getId(), userService.getCurrentUser().getId());
        }
    }

    @Secured("ROLE_USER")
    @RequestMapping("/{author}/{slug}/visible/{visibility}")
    public Object setVisible(@PathVariable Object author, @PathVariable Object slug, @PathVariable Object visibility) {
        return null; // TODO implement setVisible request controller
    }

    @GetMapping("/{author}/{slug}/watchers")
    public ModelAndView showWatchers(@PathVariable String author, @PathVariable String slug, @RequestParam(required = false, defaultValue = "1") Integer page) {
        return showUserGrid(author, slug, page, "Watchers", projectService::getProjectWatchers);
    }

    @Secured("ROLE_USER")
    @PostMapping("/{author}/{slug}/watchers/{watching}")
    @ResponseStatus(HttpStatus.OK)
    public void setWatching(@PathVariable String author, @PathVariable String slug, @PathVariable boolean watching) {
        ProjectData projectData = projectService.getProjectData(author, slug);
        ScopedProjectData scopedProjectData = projectService.getScopedProjectData(projectData.getProject().getId());
        if (scopedProjectData.isWatching() == watching) return; // No change
        if (watching) {
            userDao.get().setWatching(projectData.getProject().getId(), userService.getCurrentUser().getId());
        } else {
            userDao.get().removeWatching(projectData.getProject().getId(), userService.getCurrentUser().getId());
        }
    }

    private ModelAndView showUserGrid(String author, String slug, Integer page, String title, TriFunction<Long, Integer, Integer, Collection<UsersTable>> getUsers) {
        ProjectData projectData = projectService.getProjectData(author, slug);
        ScopedProjectData scopedProjectData = projectService.getScopedProjectData(projectData.getProject().getId());

        int pageSize = hangarConfig.projects.getUserGridPageSize();
        int offset = (page - 1) * pageSize;

        ModelAndView mav = new ModelAndView("projects/userGrid");
        mav.addObject("title", title);
        mav.addObject("p", projectData);
        mav.addObject("sp", scopedProjectData);
        mav.addObject("users", getUsers.apply(projectData.getProject().getId(), offset, pageSize));
        mav.addObject("page", page);
        mav.addObject("pageSize", pageSize);
        return fillModel(mav);
    }

}

