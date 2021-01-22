package io.papermc.hangar.controllerold;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.papermc.hangar.config.hangar.HangarConfig;
import io.papermc.hangar.controllerold.exceptions.JsonResponseException;
import io.papermc.hangar.controllerold.forms.JoinableRoleUpdates;
import io.papermc.hangar.controllerold.forms.NewProjectForm;
import io.papermc.hangar.controllerold.forms.ProjectNameValidate;
import io.papermc.hangar.controllerold.forms.SaveProjectForm;
import io.papermc.hangar.db.customtypes.LoggedActionType;
import io.papermc.hangar.db.customtypes.LoggedActionType.ProjectContext;
import io.papermc.hangar.db.customtypes.RoleCategory;
import io.papermc.hangar.db.daoold.GeneralDao;
import io.papermc.hangar.db.daoold.HangarDao;
import io.papermc.hangar.db.daoold.ProjectDao;
import io.papermc.hangar.db.daoold.UserDao;
import io.papermc.hangar.db.modelold.OrganizationsTable;
import io.papermc.hangar.db.modelold.ProjectOwner;
import io.papermc.hangar.db.modelold.ProjectsTable;
import io.papermc.hangar.db.modelold.UserProjectRolesTable;
import io.papermc.hangar.db.modelold.UsersTable;
import io.papermc.hangar.exceptions.HangarException;
import io.papermc.hangar.model.Category;
import io.papermc.hangar.modelold.FlagReason;
import io.papermc.hangar.modelold.NamedPermission;
import io.papermc.hangar.model.NotificationType;
import io.papermc.hangar.model.Permission;
import io.papermc.hangar.model.Visibility;
import io.papermc.hangar.modelold.generated.Note;
import io.papermc.hangar.modelold.viewhelpers.ProjectData;
import io.papermc.hangar.modelold.viewhelpers.ScopedOrganizationData;
import io.papermc.hangar.modelold.viewhelpers.ScopedProjectData;
import io.papermc.hangar.modelold.viewhelpers.UserData;
import io.papermc.hangar.security.annotations.GlobalPermission;
import io.papermc.hangar.security.annotations.ProjectPermission;
import io.papermc.hangar.security.annotations.UserLock;
import io.papermc.hangar.service.ApiKeyService;
import io.papermc.hangar.service.NotificationService;
import io.papermc.hangar.service.OrgService;
import io.papermc.hangar.service.RoleService;
import io.papermc.hangar.service.StatsService;
import io.papermc.hangar.service.UserActionLogService;
import io.papermc.hangar.service.UserService;
import io.papermc.hangar.service.pluginupload.ProjectFiles;
import io.papermc.hangar.service.project.FlagService;
import io.papermc.hangar.service.project.PagesSerivce;
import io.papermc.hangar.service.project.ProjectFactory;
import io.papermc.hangar.service.project.ProjectService;
import io.papermc.hangar.util.AlertUtil;
import io.papermc.hangar.util.AlertUtil.AlertType;
import io.papermc.hangar.util.FileUtils;
import io.papermc.hangar.util.Routes;
import io.papermc.hangar.util.StringUtils;
import io.papermc.hangar.util.TemplateHelper;
import io.papermc.hangar.util.TriFunction;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.websocket.server.PathParam;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.regex.Pattern;

@Controller("oldProjectsController")
public class ProjectsController extends HangarController {

    public static final Pattern ID_PATTERN = Pattern.compile("[a-z][a-z0-9-_]{0,63}");
    private static final String STATUS_DECLINE = "decline";
    private static final String STATUS_ACCEPT = "accept";
    private static final String STATUS_UNACCEPT = "unaccept";

    private final HangarConfig hangarConfig;
    private final UserService userService;
    private final OrgService orgService;
    private final FlagService flagService;
    private final ProjectService projectService;
    private final ProjectFactory projectFactory;
    private final PagesSerivce pagesSerivce;
    private final ApiKeyService apiKeyService;
    private final RoleService roleService;
    private final NotificationService notificationService;
    private final UserActionLogService userActionLogService;
    private final StatsService statsService;
    private final ProjectFiles projectFiles;
    private final TemplateHelper templateHelper;
    private final HangarDao<UserDao> userDao;
    private final HangarDao<ProjectDao> projectDao;
    private final HangarDao<GeneralDao> generalDao;

    private final HttpServletRequest request;
    private final Supplier<ProjectsTable> projectsTable;
    private final Supplier<ProjectData> projectData;

    @Autowired
    public ProjectsController(HangarConfig hangarConfig, UserService userService, OrgService orgService, FlagService flagService, ProjectService projectService, ProjectFactory projectFactory, PagesSerivce pagesSerivce, ApiKeyService apiKeyService, RoleService roleService, NotificationService notificationService, UserActionLogService userActionLogService, StatsService statsService, ProjectFiles projectFiles, TemplateHelper templateHelper, HangarDao<UserDao> userDao, HangarDao<ProjectDao> projectDao, HangarDao<GeneralDao> generalDao, HttpServletRequest request, Supplier<ProjectsTable> projectsTable, Supplier<ProjectData> projectData) {
        this.hangarConfig = hangarConfig;
        this.userService = userService;
        this.orgService = orgService;
        this.flagService = flagService;
        this.projectService = projectService;
        this.projectFactory = projectFactory;
        this.pagesSerivce = pagesSerivce;
        this.apiKeyService = apiKeyService;
        this.roleService = roleService;
        this.notificationService = notificationService;
        this.userActionLogService = userActionLogService;
        this.statsService = statsService;
        this.projectFiles = projectFiles;
        this.templateHelper = templateHelper;
        this.userDao = userDao;
        this.projectDao = projectDao;
        this.generalDao = generalDao;
        this.request = request;
        this.projectsTable = projectsTable;
        this.projectData = projectData;
    }

    @UserLock
    @Secured("ROLE_USER")
    @PostMapping(value = "/new", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public Object createProject(NewProjectForm newProjectForm) {
        // check if creation should be prevented
        UsersTable curUser = getCurrentUser();
        String uploadError = projectFactory.getUploadError(curUser);
        if (uploadError != null) {
            ModelAndView mav = showCreator();
            AlertUtil.showAlert(mav, AlertUtil.AlertType.ERROR, uploadError);
            return fillModel(mav);
        }
        // validate input
        Category category = Category.fromTitle(newProjectForm.getCategory());
        if (category == null) {
            ModelAndView mav = showCreator();
            AlertUtil.showAlert(mav, AlertUtil.AlertType.ERROR, "error.project.categoryNotFound");
            return fillModel(mav);
        }

        ProjectOwner ownerUser = getProjectOwner(newProjectForm.getOwner());
        if (ownerUser == null) {
            return fillModel(AlertUtil.showAlert(Routes.PROJECTS_SHOW_CREATOR.getRedirect(), AlertType.ERROR, "error.project.ownerNotFound"));
        }

        // create project
        ProjectsTable project;
        try {
            project = projectFactory.createProject(ownerUser, category, newProjectForm);
        } catch (HangarException ex) {
            ModelAndView mav = showCreator();
            AlertUtil.showAlert(mav, AlertUtil.AlertType.ERROR, ex.getMessageKey(), ex.getArgs());
            return fillModel(mav);
        }

        // refresh home page
        generalDao.get().refreshHomeProjects();

        return Routes.PROJECTS_SHOW.getRedirect(project.getOwnerName(), project.getSlug());
    }

    private ProjectOwner getProjectOwner(long owner) {
        UsersTable curUser = getCurrentUser();
        ProjectOwner ownerUser;
        if (owner != curUser.getId()) {
            List<OrganizationsTable> orgs = userService.getOrganizationsUserCanUploadTo(curUser);
            Optional<ProjectOwner> ownerOptional = orgs.stream().filter(org -> org.getId() == owner).map(ProjectOwner.class::cast).findAny();
            if (ownerOptional.isEmpty()) {
                return null;
            } else {
                ownerUser = ownerOptional.get();
            }
        } else {
            ownerUser = curUser;
        }
        return ownerUser;
    }

    @Secured("ROLE_USER")
    @PostMapping("/invite/{id}/{status}")
    @ResponseStatus(HttpStatus.OK)
    public void setInviteStatus(@PathVariable long id, @PathVariable String status) {
        UserProjectRolesTable projectRole = roleService.getUserProjectRole(id);
        if (projectRole == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        updateRole(status, projectRole);
    }

    @Secured("ROLE_USER")
    @PostMapping("/invite/{id}/{status}/{behalf}")
    @ResponseStatus(HttpStatus.OK)
    public void setInviteStatusOnBehalf(@PathVariable long id, @PathVariable String status, @PathVariable String behalf) {
        UserProjectRolesTable projectRole = roleService.getUserProjectRole(id);
        OrganizationsTable organizationsTable = orgService.getOrganization(behalf);
        if (projectRole == null || organizationsTable == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        ScopedOrganizationData scopedOrganizationData = orgService.getScopedOrganizationData(organizationsTable);
        if (!scopedOrganizationData.getPermissions().has(Permission.ManageProjectMembers)) {
            if (getCurrentUser() != null) { // getCurrentUser() handles throwing UNAUTHORIZED
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }
        }
        updateRole(status, projectRole);
    }

    private void updateRole(String status, UserProjectRolesTable projectRole) {
        switch (status) {
            case STATUS_DECLINE:
                roleService.removeRole(projectRole);
                break;
            case STATUS_ACCEPT:
                projectRole.setIsAccepted(true);
                roleService.updateRole(projectRole);
                break;
            case STATUS_UNACCEPT:
                projectRole.setIsAccepted(false);
                roleService.updateRole(projectRole);
                break;
            default:
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    @Secured("ROLE_USER")
    @PostMapping(value = "/validateProjectName", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public void validateProjectName(@RequestBody ProjectNameValidate projectNameValidateForm) {
        ProjectOwner projectOwner = getProjectOwner(projectNameValidateForm.getOwnerId());
        try {
            projectFactory.checkProjectAvailability(projectOwner, projectNameValidateForm.getProjectName());
        } catch (HangarException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    @UserLock
    @Secured("ROLE_USER")
    @GetMapping("/new")
    public ModelAndView showCreator() {
        ModelAndView mav = new ModelAndView("projects/create");
        mav.addObject("createProjectOrgas", orgService.getOrgsWithPerm(getCurrentUser(), Permission.CreateProject));
        return fillModel(mav);
    }

    @GetMapping("/{author}/{slug}")
    public ModelAndView show(@PathVariable String author, @PathVariable String slug, @PathParam("donation") String donation) {
        ModelAndView mav = new ModelAndView("projects/pages/view");
        ProjectData projData = projectData.get();
        mav.addObject("p", projData);
        ScopedProjectData sp = projectService.getScopedProjectData(projData.getProject().getId());
        mav.addObject("sp", sp);
        mav.addObject("projectPage", pagesSerivce.getPage(projData.getProject().getId(), hangarConfig.pages.home.getName()));
        mav.addObject("editorOpen", false);
        pagesSerivce.fillPages(mav, projData.getProject().getId());
        statsService.addProjectView(projData.getProject());
        if (donation != null) {
            mav.addObject("donation", donation);
        }
        return fillModel(mav);
    }

    @GetMapping("/{author}/{slug}/discuss")
    public ModelAndView showDiscussion(@PathVariable String author, @PathVariable String slug) {
        ModelAndView mv = new ModelAndView("projects/discuss");
        ProjectData projData = projectData.get();
        ScopedProjectData scopedProjectData = projectService.getScopedProjectData(projData.getProject().getId());
        mv.addObject("p", projData);
        mv.addObject("sp", scopedProjectData);
        statsService.addProjectView(projData.getProject()); // TODO this is in ore, but I'm not sure why
        return fillModel(mv);
    }

    @Secured("ROLE_USER")
    @PostMapping("/{author}/{slug}/discuss/reply")
    public Object postDiscussionReply(@PathVariable String author, @PathVariable String slug) {
        return null; // TODO implement postDiscussionReply request controller
    }

    @Secured("ROLE_USER")
    @PostMapping(value = "/{author}/{slug}/flag", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ModelAndView flag(@PathVariable String author, @PathVariable String slug, @RequestParam("flag-reason") FlagReason flagReason, @RequestParam String comment) {
        ProjectsTable project = projectsTable.get();
        if (flagService.hasUnresolvedFlag(project.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only 1 flag at a time per project per user");
        } else if (comment.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Comment must not be blank");
        }
        flagService.flagProject(project.getId(), flagReason, comment);
        String userName = getCurrentUser().getName();
        userActionLogService.project(request, LoggedActionType.PROJECT_FLAGGED.with(ProjectContext.of(project.getId())), "Flagged by " + userName, "Not flagged by " + userName);
        return Routes.PROJECTS_SHOW.getRedirect(author, slug); // TODO flashing
    }

    @GlobalPermission(NamedPermission.MOD_NOTES_AND_FLAGS)
    @Secured("ROLE_USER")
    @GetMapping("/{author}/{slug}/flags")
    public ModelAndView showFlags(@PathVariable String author, @PathVariable String slug) {
        ModelAndView mav = new ModelAndView("projects/admin/flags");
        mav.addObject("p", projectData.get());
        return fillModel(mav);
    }

    @UserLock(route = Routes.PROJECTS_SHOW, args = "{#author, #slug}")
    @Secured("ROLE_USER")
    @ProjectPermission(NamedPermission.EDIT_SUBJECT_SETTINGS)
    @PostMapping(value = "/{author}/{slug}/icon", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Object uploadIcon(@PathVariable String author, @PathVariable String slug, @RequestParam MultipartFile icon, RedirectAttributes attributes) {
        ProjectsTable project = projectsTable.get();
        if (icon.getContentType() == null || (!icon.getContentType().equals(MediaType.IMAGE_PNG_VALUE) && !icon.getContentType().equals(MediaType.IMAGE_JPEG_VALUE))) {
            AlertUtil.showAlert(attributes, AlertUtil.AlertType.ERROR, "error.invalidFile");
            return Routes.PROJECTS_SHOW_SETTINGS.getRedirect(author, slug);
        }
        if (icon.getOriginalFilename() == null || icon.getOriginalFilename().isBlank()) {
            AlertUtil.showAlert(attributes, AlertUtil.AlertType.ERROR, "error.noFile");
            return Routes.PROJECTS_SHOW_SETTINGS.getRedirect(author, slug);
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

    @ProjectPermission(NamedPermission.EDIT_SUBJECT_SETTINGS)
    @UserLock(route = Routes.PROJECTS_SHOW, args = "{#author, #slug}")
    @Secured("ROLE_USER")
    @PostMapping("/{author}/{slug}/icon/reset")
    @ResponseStatus(HttpStatus.OK)
    public void resetIcon(@PathVariable String author, @PathVariable String slug) {
        ProjectsTable project = projectsTable.get();
        Path icon = projectFiles.getIconPath(author, slug);
        Path pendingIcon = projectFiles.getPendingIconPath(author, slug);
        FileUtils.delete(icon);
        FileUtils.delete(pendingIcon);

        // TODO data
        userActionLogService.project(request, LoggedActionType.PROJECT_ICON_CHANGED.with(ProjectContext.of(project.getId())), "", "");
    }

    @UserLock(route = Routes.PROJECTS_SHOW, args = "{#author, #slug}")
    @ProjectPermission(NamedPermission.EDIT_SUBJECT_SETTINGS)
    @Secured("ROLE_USER")
    @GetMapping("/{author}/{slug}/manage")
    public ModelAndView showSettings(@PathVariable String author, @PathVariable String slug) {
        ModelAndView mav = new ModelAndView("projects/settings");
        ProjectData projData = projectData.get();
        mav.addObject("p", projData);
        ScopedProjectData scopedProjectData = projectService.getScopedProjectData(projData.getProject().getId());
        mav.addObject("sp", scopedProjectData);
        mav.addObject("iconUrl", templateHelper.projectAvatarUrl(projData.getProject()));
        mav.addObject("deploymentKey", apiKeyService.getProjectKeys(projData.getProject().getId()).stream().findFirst().orElse(null));
        return fillModel(mav);
    }

    @ProjectPermission(NamedPermission.DELETE_PROJECT)
    @UserLock(route = Routes.PROJECTS_SHOW, args = "{#author, #slug}")
    @Secured("ROLE_USER")
    @PostMapping(value = "/{author}/{slug}/manage/delete", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ModelAndView softDelete(@PathVariable String author, @PathVariable String slug, @RequestParam(required = false) String comment, RedirectAttributes ra) {
        ProjectsTable project = projectsTable.get();
        Visibility oldVisibility = project.getVisibility();

        userActionLogService.project(request, LoggedActionType.PROJECT_VISIBILITY_CHANGE.with(ProjectContext.of(project.getId())), Visibility.SOFTDELETE.getName(), oldVisibility.getName());
        projectFactory.softDeleteProject(project, comment);
        AlertUtil.showAlert(ra, AlertUtil.AlertType.SUCCESS, "project.deleted", project.getName());
        projectService.refreshHomePage();
        return Routes.SHOW_HOME.getRedirect();
    }

    @GlobalPermission(NamedPermission.HARD_DELETE_PROJECT)
    @Secured("ROLE_USER")
    @PostMapping("/{author}/{slug}/manage/hardDelete")
    public ModelAndView delete(@PathVariable String author, @PathVariable String slug, RedirectAttributes ra) {
        ProjectsTable project = projectsTable.get();
        projectFactory.hardDeleteProject(project);
        userActionLogService.project(request, LoggedActionType.PROJECT_VISIBILITY_CHANGE.with(ProjectContext.of(project.getId())), "deleted", project.getVisibility().getName());
        AlertUtil.showAlert(ra, AlertUtil.AlertType.SUCCESS, "project.deleted", project.getName());
        projectService.refreshHomePage();
        return Routes.SHOW_HOME.getRedirect();
    }

    @UserLock(route = Routes.PROJECTS_SHOW, args = "{#author, #slug}")
    @Secured("ROLE_USER")
    @ProjectPermission(NamedPermission.MANAGE_SUBJECT_MEMBERS)
    @PostMapping("/{author}/{slug}/manage/members/remove")
    public ModelAndView removeMember(@PathVariable String author, @PathVariable String slug, @RequestParam String username) {
        ProjectData projData = projectData.get();
        UserData user = userService.getUserData(username);
        if (user == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        if (roleService.removeMember(projData.getProject(), user.getUser().getId()) != 0) {
            userActionLogService.project(
                    request,
                    LoggedActionType.PROJECT_MEMBER_REMOVED.with(ProjectContext.of(projData.getProject().getId())),
                    user.getUser().getName() + " is not a member of " + projData.getNamespace(),
                    user.getUser().getName() + " is a member of " + projData.getNamespace());
        }
        return Routes.PROJECTS_SHOW_SETTINGS.getRedirect(author, slug);
    }

    @ProjectPermission(NamedPermission.EDIT_SUBJECT_SETTINGS)
    @UserLock(route = Routes.PROJECTS_SHOW, args = "{#author, #slug}")
    @Secured("ROLE_USER")
    @PostMapping(value = "/{author}/{slug}/manage/rename", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public Object rename(@PathVariable String author, @PathVariable String slug, @RequestParam("name") String newName) {
        String compactNewName = StringUtils.compact(newName);

        ProjectData projData = projectData.get();
        String oldName = projData.getProject().getName();
        try {
            projectFactory.checkProjectAvailability(projData.getProjectOwner(), compactNewName);
        } catch (HangarException e) {
            ModelAndView mav = showSettings(author, slug);
            AlertUtil.showAlert(mav, AlertUtil.AlertType.ERROR, "error.nameUnavailable");
            AlertUtil.showAlert(mav, AlertUtil.AlertType.ERROR, e.getMessageKey());
            return mav;
        }

        projData.getProject().setName(compactNewName);
        projData.getProject().setSlug(StringUtils.slugify(compactNewName));
        projectDao.get().update(projData.getProject());
        userActionLogService.project(request, LoggedActionType.PROJECT_RENAMED.with(ProjectContext.of(projData.getProject().getId())), author + "/" + compactNewName, author + "/" + oldName);
        projectService.refreshHomePage();
        return Routes.PROJECTS_SHOW.getRedirect(author, newName);
    }

    @ProjectPermission(NamedPermission.EDIT_SUBJECT_SETTINGS)
    @UserLock(route = Routes.PROJECTS_SHOW, args = "{#author, #slug}")
    @Secured("ROLE_USER")
    @PostMapping(value = "/{author}/{slug}/manage/save", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public void save(@PathVariable String author, @PathVariable String slug, @RequestBody SaveProjectForm saveProjectForm) {
        if (saveProjectForm.getKeywords().size() > hangarConfig.getProjects().getMaxKeywords()) {
            throw new JsonResponseException("error.project.maxKeywords", hangarConfig.getProjects().getMaxKeywords());
        }
        ProjectsTable project = projectsTable.get();
        project.setCategory(saveProjectForm.getCategory());
        project.setKeywords(saveProjectForm.getKeywords());
        project.setHomepage(saveProjectForm.getProjectLinks().getHomepage());
        project.setIssues(saveProjectForm.getProjectLinks().getIssues());
        project.setSource(saveProjectForm.getProjectLinks().getSource());
        project.setSupport(saveProjectForm.getProjectLinks().getSupport());
        project.setLicenseName(saveProjectForm.getLicense().getName());
        project.setLicenseUrl(saveProjectForm.getLicense().getUrl());
        project.setForumSync(saveProjectForm.isForumSync());
        project.setDescription(saveProjectForm.getDescription());
        projectDao.get().update(project);

        if (saveProjectForm.isIconChange()) {
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

        JoinableRoleUpdates projectRoleUpdates = saveProjectForm.getProjectRoleUpdates();
        projectRoleUpdates.getAdditions().forEach(userRole -> {
            if (userRole.getRole().getCategory() == RoleCategory.PROJECT & userRole.getRole().isAssignable()) {
                roleService.addRole(project, userRole.getUserId(), userRole.getRole(), false);
                notificationService.sendNotification(userRole.getUserId(), project.getOwnerId(), NotificationType.PROJECT_INVITE,new String[]{"notification.project.invite", userRole.getRole().getTitle(), project.getName()});
            }
        });
        projectRoleUpdates.getUpdates().forEach(userRole -> {
            if (userRole.getRole().getCategory() == RoleCategory.PROJECT && userRole.getRole().isAssignable()) {
                roleService.updateRole(project, userRole.getUserId(), userRole.getRole());
            }
        });

        projectService.refreshHomePage();
        userActionLogService.project(request, LoggedActionType.PROJECT_SETTINGS_CHANGED.with(ProjectContext.of(project.getId())), "", "");
    }

    @ProjectPermission(NamedPermission.EDIT_SUBJECT_SETTINGS)
    @UserLock(route = Routes.PROJECTS_SHOW, args = "{#author, #slug}")
    @Secured("ROLE_USER")
    @GetMapping("/{author}/{slug}/manage/sendforapproval")
    public ModelAndView sendForApproval(@PathVariable String author, @PathVariable String slug) {
        ProjectsTable project = projectsTable.get();
        if (project.getVisibility() == Visibility.NEEDSCHANGES) {
            projectService.changeVisibility(project, Visibility.NEEDSAPPROVAL, "");
            userActionLogService.project(request, LoggedActionType.PROJECT_VISIBILITY_CHANGE.with(ProjectContext.of(project.getId())), Visibility.NEEDSAPPROVAL.getName(), Visibility.NEEDSCHANGES.getName());
        }
        return Routes.PROJECTS_SHOW.getRedirect(author, slug);
    }

    @GlobalPermission(NamedPermission.MOD_NOTES_AND_FLAGS)
    @Secured("ROLE_USER")
    @GetMapping("/{author}/{slug}/notes")
    public ModelAndView showNotes(@PathVariable String author, @PathVariable String slug) {
        ModelAndView mv = new ModelAndView("projects/admin/notes");
        ProjectsTable project = projectsTable.get();

        List<Note> notes = new ArrayList<>();
        ArrayNode messages = (ArrayNode) project.getNotes().getJson().get("messages");
        if (messages != null) {
            for (JsonNode message : messages) {
                Note note = new Note().message(message.get("message").asText());
                notes.add(note);
                UserData user = userService.getUserData(message.get("user").asLong());
                note.user(user.getUser().getName());
            }
        }

        mv.addObject("project", project);
        mv.addObject("notes", notes);
        return fillModel(mv);
    }

    @GlobalPermission(NamedPermission.MOD_NOTES_AND_FLAGS)
    @Secured("ROLE_USER")
    @PostMapping("/{author}/{slug}/notes/addmessage")
    public ResponseEntity<String> addMessage(@PathVariable String author, @PathVariable String slug, @RequestParam String content) {
        ProjectsTable project = projectsTable.get();
        ArrayNode messages = project.getNotes().getJson().withArray("messages");
        ObjectNode note = messages.addObject();
        note.put("message", content);
        note.put("user", getCurrentUser().getId());

        String json = project.getNotes().getJson().toString();
        projectDao.get().updateNotes(json, project.getId());
        return ResponseEntity.ok("Review");
    }

    @GetMapping("/{author}/{slug}/stars")
    public ModelAndView showStargazers(@PathVariable String author, @PathVariable String slug, @RequestParam(required = false, defaultValue = "1") Integer page) {
        return showUserGrid(page, "Stargazers", projectService::getProjectStargazers);
    }

    @Secured("ROLE_USER")
    @PostMapping("/{author}/{slug}/stars/toggle")
    @ResponseStatus(HttpStatus.OK)
    public void toggleStarred(@PathVariable String author, @PathVariable String slug) {
        UsersTable curUser = getCurrentUser();
        ProjectsTable project = projectsTable.get();
        ScopedProjectData scopedProjectData = projectService.getScopedProjectData(project.getId());
        if (scopedProjectData.isStarred()) {
            userDao.get().removeStargazing(project.getId(), curUser.getId());
        } else {
            userDao.get().setStargazing(project.getId(), curUser.getId());
        }
    }

    @GlobalPermission(NamedPermission.REVIEWER)
    @UserLock(route = Routes.PROJECTS_SHOW, args = "{#author, #slug}")
    @Secured("ROLE_USER")
    @PostMapping(value = "/{author}/{slug}/visible/{visibility}", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public void setVisible(@PathVariable String author,
                           @PathVariable String slug,
                           @PathVariable Visibility visibility,
                           @RequestParam(required = false) String comment) {
        ProjectsTable project = projectsTable.get();
        Visibility oldVisibility = project.getVisibility();
        projectService.changeVisibility(project, visibility, comment);
        userActionLogService.project(request, LoggedActionType.PROJECT_VISIBILITY_CHANGE.with(ProjectContext.of(project.getId())), visibility.getName(), oldVisibility.getName());
        projectService.refreshHomePage();
    }

    @GetMapping("/{author}/{slug}/watchers")
    public ModelAndView showWatchers(@PathVariable String author, @PathVariable String slug, @RequestParam(required = false, defaultValue = "1") Integer page) {
        return showUserGrid(page, "Watchers", projectService::getProjectWatchers);
    }

    @Secured("ROLE_USER")
    @PostMapping("/{author}/{slug}/watchers/{watching}")
    @ResponseStatus(HttpStatus.OK)
    public void setWatching(@PathVariable String author, @PathVariable String slug, @PathVariable boolean watching) {
        UsersTable curUser = getCurrentUser();
        ProjectsTable project = projectsTable.get();
        ScopedProjectData scopedProjectData = projectService.getScopedProjectData(project.getId());
        if (scopedProjectData.isWatching() == watching) return; // No change
        if (watching) {
            userDao.get().setWatching(project.getId(), curUser.getId());
        } else {
            userDao.get().removeWatching(project.getId(), curUser.getId());
        }
    }

    private ModelAndView showUserGrid(Integer page, String title, TriFunction<Long, Integer, Integer, Collection<UsersTable>> getUsers) {
        ProjectData projData = projectData.get();
        ScopedProjectData scopedProjectData = projectService.getScopedProjectData(projData.getProject().getId());

        int pageSize = hangarConfig.projects.getUserGridPageSize();
        int offset = (page - 1) * pageSize;

        ModelAndView mav = new ModelAndView("projects/userGrid");
        mav.addObject("title", title);
        mav.addObject("p", projData);
        mav.addObject("sp", scopedProjectData);
        mav.addObject("users", getUsers.apply(projData.getProject().getId(), offset, pageSize));
        mav.addObject("page", page);
        mav.addObject("pageSize", pageSize);
        return fillModel(mav);
    }

}

