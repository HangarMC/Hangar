package me.minidigger.hangar.controller;

import me.minidigger.hangar.config.hangar.HangarConfig;
import me.minidigger.hangar.db.customtypes.LoggedActionType;
import me.minidigger.hangar.db.customtypes.LoggedActionType.ProjectContext;
import me.minidigger.hangar.db.dao.HangarDao;
import me.minidigger.hangar.db.dao.ProjectDao;
import me.minidigger.hangar.db.dao.UserDao;
import me.minidigger.hangar.db.model.ProjectsTable;
import me.minidigger.hangar.db.model.UsersTable;
import me.minidigger.hangar.model.Category;
import me.minidigger.hangar.model.FlagReason;
import me.minidigger.hangar.model.NamedPermission;
import me.minidigger.hangar.model.Permission;
import me.minidigger.hangar.model.Visibility;
import me.minidigger.hangar.model.generated.Note;
import me.minidigger.hangar.model.viewhelpers.ProjectData;
import me.minidigger.hangar.model.viewhelpers.ProjectPage;
import me.minidigger.hangar.model.viewhelpers.ScopedProjectData;
import me.minidigger.hangar.security.annotations.GlobalPermission;
import me.minidigger.hangar.security.annotations.ProjectPermission;
import me.minidigger.hangar.service.OrgService;
import me.minidigger.hangar.service.UserActionLogService;
import me.minidigger.hangar.service.UserService;
import me.minidigger.hangar.service.pluginupload.ProjectFiles;
import me.minidigger.hangar.service.project.FlagService;
import me.minidigger.hangar.service.project.PagesSerivce;
import me.minidigger.hangar.service.project.ProjectFactory;
import me.minidigger.hangar.service.project.ProjectService;
import me.minidigger.hangar.util.AlertUtil;
import me.minidigger.hangar.util.AlertUtil.AlertType;
import me.minidigger.hangar.util.FileUtils;
import me.minidigger.hangar.util.HangarException;
import me.minidigger.hangar.util.RouteHelper;
import me.minidigger.hangar.util.StringUtils;
import me.minidigger.hangar.util.TemplateHelper;
import me.minidigger.hangar.util.TriFunction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@Controller
public class ProjectsController extends HangarController {

    public static final Pattern ID_PATTERN = Pattern.compile("[a-z][a-z0-9-_]{0,63}");

    private final HangarConfig hangarConfig;
    private final RouteHelper routeHelper;
    private final UserService userService;
    private final OrgService orgService;
    private final FlagService flagService;
    private final ProjectService projectService;
    private final ProjectFactory projectFactory;
    private final PagesSerivce pagesSerivce;
    private final UserActionLogService userActionLogService;
    private final ProjectFiles projectFiles;
    private final TemplateHelper templateHelper;
    private final HangarDao<UserDao> userDao;
    private final HangarDao<ProjectDao> projectDao;

    private final HttpServletRequest request;

    @Autowired
    public ProjectsController(HangarConfig hangarConfig, RouteHelper routeHelper, UserService userService, OrgService orgService, FlagService flagService, ProjectService projectService, ProjectFactory projectFactory, PagesSerivce pagesSerivce, UserActionLogService userActionLogService, ProjectFiles projectFiles, TemplateHelper templateHelper, HangarDao<UserDao> userDao, HangarDao<ProjectDao> projectDao, HttpServletRequest request) {
        this.hangarConfig = hangarConfig;
        this.routeHelper = routeHelper;
        this.userService = userService;
        this.orgService = orgService;
        this.flagService = flagService;
        this.projectService = projectService;
        this.projectFactory = projectFactory;
        this.pagesSerivce = pagesSerivce;
        this.userActionLogService = userActionLogService;
        this.projectFiles = projectFiles;
        this.templateHelper = templateHelper;
        this.userDao = userDao;
        this.projectDao = projectDao;
        this.request = request;
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
        mav.addObject("parentPage"); // TODO parent page
        mav.addObject("editorOpen", false);
        pagesSerivce.fillPages(mav, projectData.getProject().getId());
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
    @PostMapping(value = "/{author}/{slug}/flag", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ModelAndView flag(@PathVariable String author, @PathVariable String slug, @RequestParam("flag-reason") FlagReason flagReason, @RequestParam String comment) {
        ProjectData projectData = projectService.getProjectData(author, slug);
        if (flagService.hasUnresolvedFlag(projectData.getProject().getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only 1 flag at a time per project per user");
        } else if (comment.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Comment must not be blank");
        }
        flagService.flagProject(projectData.getProject().getId(), flagReason, comment);
        String userName = userService.getCurrentUser().getName();
        userActionLogService.project(request, LoggedActionType.PROJECT_FLAGGED.with(ProjectContext.of(projectData.getProject().getId())), "Flagged by " + userName, "Not flagged by " + userName);
        return new ModelAndView("redirect:" + routeHelper.getRouteUrl("projects.show", author, slug)); // TODO flashing
    }

    @Secured("ROLE_USER")
    @GetMapping("/{author}/{slug}/flags")
    public ModelAndView showFlags(@PathVariable String author, @PathVariable String slug) {
        ModelAndView mav = new ModelAndView("projects/admin/flags");
        mav.addObject("p", projectService.getProjectData(author, slug));
        return fillModel(mav);
    }

    @Secured("ROLE_USER")
    @ProjectPermission(NamedPermission.EDIT_SUBJECT_SETTINGS)
    @PostMapping(value = "/{author}/{slug}/icon", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Object uploadIcon(@PathVariable String author, @PathVariable String slug, @RequestParam MultipartFile icon, RedirectAttributes attributes) {
        ProjectsTable project = projectService.getProjectData(author, slug).getProject();
        if (icon.getContentType() == null || (!icon.getContentType().equals(MediaType.IMAGE_PNG_VALUE) && !icon.getContentType().equals(MediaType.IMAGE_JPEG_VALUE))) {
            AlertUtil.showAlert(attributes, AlertType.ERROR, "error.invalidFile");
            return new ModelAndView("redirect:" + routeHelper.getRouteUrl("projects.showSettings", author, slug));
        }
        if (icon.getOriginalFilename() == null || icon.getOriginalFilename().isBlank()) {
            AlertUtil.showAlert(attributes, AlertType.ERROR, "error.noFile");
            return new ModelAndView("redirect:" + routeHelper.getRouteUrl("projects.showSettings", author, slug));
        }
        try {
            Path pendingDir = projectFiles.getPendingIconDir(author, slug);
            if (Files.notExists(pendingDir)) {
                Files.createDirectories(pendingDir);
            }
            FileUtils.deletedFiles(pendingDir);
            Files.copy(icon.getInputStream(), pendingDir.resolve(icon.getOriginalFilename()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        // todo data
        userActionLogService.project(request, LoggedActionType.PROJECT_ICON_CHANGED.with(ProjectContext.of(project.getId())), "", "");
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/{author}/{slug}/icon")
    public Object showIcon(@PathVariable String author, @PathVariable String slug) {
        Path iconPath = projectFiles.getIconPath(author, slug);
        if (iconPath == null) {
            return new ModelAndView("redirect:" + templateHelper.avatarUrl(author));
        }
        return showImage(iconPath);
    }

    @GetMapping("/{author}/{slug}/icon/pending")
    public ResponseEntity<byte[]> showPendingIcon(@PathVariable String author, @PathVariable String slug) {
        Path path = projectFiles.getPendingIconPath(author, slug);
        if (path == null) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        return showImage(path);
    }

    private ResponseEntity<byte[]> showImage(Path path) {
        try {
            byte[] lastModified = Files.getLastModifiedTime(path).toString().getBytes(StandardCharsets.UTF_8);
            byte[] lastModifiedHash = MessageDigest.getInstance("MD5").digest(lastModified);
            String hashString = Base64.getEncoder().encodeToString(lastModifiedHash);
            return ResponseEntity.ok().header(HttpHeaders.ETAG, hashString).header(HttpHeaders.CACHE_CONTROL, "max-age=" + 3600).body(Files.readAllBytes(path));
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Secured("ROLE_USER")
    @PostMapping("/{author}/{slug}/icon/reset")
    @ResponseStatus(HttpStatus.OK)
    public void resetIcon(@PathVariable String author, @PathVariable String slug) {
        ProjectsTable projectsTable = projectService.getProjectData(author, slug).getProject();
        Path icon = projectFiles.getIconPath(author, slug);
        Path pendingIcon = projectFiles.getPendingIconPath(author, slug);
        FileUtils.delete(icon);
        FileUtils.delete(pendingIcon);

        // TODO data
        userActionLogService.project(request, LoggedActionType.PROJECT_ICON_CHANGED.with(ProjectContext.of(projectsTable.getId())), "", "");
    }

    @Secured("ROLE_USER")
    @RequestMapping("/{author}/{slug}/manage")
    public ModelAndView showSettings(@PathVariable String author, @PathVariable String slug) {
        ModelAndView mav = new ModelAndView("projects/settings");
        ProjectData projectData = projectService.getProjectData(author, slug);
        mav.addObject("p", projectData);
        ScopedProjectData scopedProjectData = projectService.getScopedProjectData(projectData.getProject().getId());
        mav.addObject("sp", scopedProjectData);
        mav.addObject("iconUrl", templateHelper.projectAvatarUrl(projectData.getProject()));
        // TODO add deploymentKey
        return fillModel(mav);
    }

    @Secured("ROLE_USER")
    @PostMapping(value = "/{author}/{slug}/manage/delete", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public RedirectView softDelete(@PathVariable String author, @PathVariable String slug, @RequestParam(required = false) String comment, RedirectAttributes ra) {
        ProjectData projectData = projectService.getProjectData(author, slug);
        Visibility oldVisibility = projectData.getVisibility();

        userActionLogService.project(request, LoggedActionType.PROJECT_VISIBILITY_CHANGE.with(ProjectContext.of(projectData.getProject().getId())), Visibility.SOFTDELETE.getName(), oldVisibility.getName());
        projectFactory.softDeleteProject(projectData, comment);
        AlertUtil.showAlert(ra, AlertType.SUCCESS, "project.deleted", projectData.getProject().getName());
        return new RedirectView(routeHelper.getRouteUrl("showHome"));
    }

    @Secured("ROLE_USER")
    @RequestMapping("/{author}/{slug}/manage/hardDelete")
    public RedirectView delete(@PathVariable String author, @PathVariable String slug, RedirectAttributes ra) {
        ProjectData projectData = projectService.getProjectData(author, slug);
        projectFactory.hardDeleteProject(projectData);
        userActionLogService.project(request, LoggedActionType.PROJECT_VISIBILITY_CHANGE.with(ProjectContext.of(projectData.getProject().getId())), "deleted", projectData.getVisibility().getName());
        AlertUtil.showAlert(ra, AlertType.SUCCESS, "project.deleted", projectData.getProject().getName());
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
        String compactNewName = StringUtils.compact(newName);

        ProjectData projectData = projectService.getProjectData(author, slug);
        String oldName = projectData.getProject().getName();
        try {
            projectFactory.checkProjectAvailability(projectData.getProjectOwner(), compactNewName);
        } catch (HangarException e) {
            ModelAndView mav = showSettings(author, slug);
            AlertUtil.showAlert(mav, AlertType.ERROR, "error.nameUnavailable");
            AlertUtil.showAlert(mav, AlertType.ERROR, e.getMessageKey());
            return mav;
        }

        projectData.getProject().setName(compactNewName);
        projectData.getProject().setSlug(StringUtils.slugify(compactNewName));
        projectDao.get().update(projectData.getProject());
        userActionLogService.project(request, LoggedActionType.PROJECT_RENAMED.with(ProjectContext.of(projectData.getProject().getId())), author + "/" + compactNewName, author + "/" + oldName);
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

        if (updateIcon) {
            Path pendingIconPath = projectFiles.getPendingIconPath(author, slug);
            if (pendingIconPath != null) {
                 try {
                    Path iconDir = projectFiles.getIconDir(author, slug);
                    if (Files.notExists(iconDir)) {
                        Files.createDirectories(iconDir);
                    }
                     FileUtils.deletedFiles(iconDir);
                    Files.move(pendingIconPath, iconDir.resolve(pendingIconPath.getFileName()));
                } catch (IOException e) {
                     e.printStackTrace();
                 }
            }
        }

        // TODO add new roles
        // TODO update existing roles
        userActionLogService.project(request, LoggedActionType.PROJECT_SETTINGS_CHANGED.with(ProjectContext.of(projectsTable.getId())), "", "");

        return new RedirectView(routeHelper.getRouteUrl("projects.show", author, slug));
    }

    @Secured("ROLE_USER")
    @RequestMapping("/{author}/{slug}/manage/sendforapproval")
    public ModelAndView sendForApproval(@PathVariable String author, @PathVariable String slug) {
        ProjectData projectData = projectService.getProjectData(author, slug);
        if (projectData.getVisibility() == Visibility.NEEDSCHANGES) {
            projectService.changeVisibility(projectData.getProject(), Visibility.NEEDSAPPROVAL, "");
            userActionLogService.project(request, LoggedActionType.PROJECT_VISIBILITY_CHANGE.with(ProjectContext.of(projectData.getProject().getId())), Visibility.NEEDSAPPROVAL.getName(), Visibility.NEEDSCHANGES.getName());
        }
        return new ModelAndView("redirect:" + routeHelper.getRouteUrl("projects.show", author, slug));
    }

    @GlobalPermission(NamedPermission.MOD_NOTES_AND_FLAGS)
    @Secured("ROLE_USER")
    @RequestMapping("/{author}/{slug}/notes")
    public ModelAndView showNotes(@PathVariable String author, @PathVariable String slug) {
        ModelAndView mv = new ModelAndView("projects/admin/notes");
        ProjectData projectData = projectService.getProjectData(author, slug);
        mv.addObject("project", projectData.getProject());
        mv.addObject("notes", List.of(new Note().message("## 10/10\n* has everything\n* but also nothing").user("kneny")));
        return fillModel(mv);
    }

    @GlobalPermission(NamedPermission.MOD_NOTES_AND_FLAGS)
    @Secured("ROLE_USER")
    @RequestMapping("/{author}/{slug}/notes/addmessage")
    public ModelAndView addMessage(@PathVariable String author, @PathVariable String slug) {
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
    @PostMapping(value = "/{author}/{slug}/visible/{visibility}", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public void setVisible(@PathVariable String author,
                           @PathVariable String slug,
                           @PathVariable Visibility visibility,
                           @RequestParam(required = false) String comment) {
        ProjectData projectData = projectService.getProjectData(author, slug);
        projectService.changeVisibility(projectData.getProject(), visibility, comment);
        // TODO user action logging
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

