package io.papermc.hangar.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.papermc.hangar.config.hangar.HangarConfig;
import io.papermc.hangar.db.customtypes.LoggedActionType;
import io.papermc.hangar.db.customtypes.LoggedActionType.ProjectContext;
import io.papermc.hangar.db.dao.GeneralDao;
import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.ProjectDao;
import io.papermc.hangar.db.dao.UserDao;
import io.papermc.hangar.db.model.OrganizationsTable;
import io.papermc.hangar.db.model.ProjectOwner;
import io.papermc.hangar.db.model.ProjectsTable;
import io.papermc.hangar.db.model.UserProjectRolesTable;
import io.papermc.hangar.db.model.UsersTable;
import io.papermc.hangar.model.Category;
import io.papermc.hangar.model.FlagReason;
import io.papermc.hangar.model.NamedPermission;
import io.papermc.hangar.model.NotificationType;
import io.papermc.hangar.model.Permission;
import io.papermc.hangar.model.Role;
import io.papermc.hangar.model.Visibility;
import io.papermc.hangar.model.generated.Note;
import io.papermc.hangar.model.viewhelpers.ProjectData;
import io.papermc.hangar.model.viewhelpers.ScopedProjectData;
import io.papermc.hangar.model.viewhelpers.UserData;
import io.papermc.hangar.security.annotations.GlobalPermission;
import io.papermc.hangar.security.annotations.ProjectPermission;
import io.papermc.hangar.service.ApiKeyService;
import io.papermc.hangar.service.NotificationService;
import io.papermc.hangar.service.OrgService;
import io.papermc.hangar.service.RoleService;
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
import io.papermc.hangar.util.HangarException;
import io.papermc.hangar.util.Routes;
import io.papermc.hangar.util.StringUtils;
import io.papermc.hangar.util.TemplateHelper;
import io.papermc.hangar.util.TriFunction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerMapping;
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
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Controller
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
    private final ProjectFiles projectFiles;
    private final TemplateHelper templateHelper;
    private final HangarDao<UserDao> userDao;
    private final HangarDao<ProjectDao> projectDao;
    private final HangarDao<GeneralDao> generalDao;

    private final HttpServletRequest request;

    @Autowired
    public ProjectsController(HangarConfig hangarConfig, UserService userService, OrgService orgService, FlagService flagService, ProjectService projectService, ProjectFactory projectFactory, PagesSerivce pagesSerivce, ApiKeyService apiKeyService, RoleService roleService, NotificationService notificationService, UserActionLogService userActionLogService, ProjectFiles projectFiles, TemplateHelper templateHelper, HangarDao<UserDao> userDao, HangarDao<ProjectDao> projectDao, HangarDao<GeneralDao> generalDao, HttpServletRequest request) {
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
        this.projectFiles = projectFiles;
        this.templateHelper = templateHelper;
        this.userDao = userDao;
        this.projectDao = projectDao;
        this.generalDao = generalDao;
        this.request = request;
    }

    @Bean
    @RequestScope
    Supplier<ProjectsTable> projectsTable() {
        Map<String, String> pathParams = (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        ProjectsTable pt;
        if (pathParams.keySet().containsAll(Set.of("author", "slug"))) {
            pt = projectService.getProjectsTable(pathParams.get("author"), pathParams.get("slug"));
        } else if (pathParams.containsKey("pluginId")) {
            pt = projectService.getProjectsTable(pathParams.get("pluginId"));
        } else {
            return () -> null;
        }
        if (pt == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return () -> pt;
    }

    @Bean
    @RequestScope
    Supplier<ProjectData> projectData() {
        //noinspection SpringConfigurationProxyMethods
        return () -> projectService.getProjectData(projectsTable().get());
    }

    @Secured("ROLE_USER")
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
            AlertUtil.showAlert(mav, AlertUtil.AlertType.ERROR, uploadError);
            return fillModel(mav);
        }
        // validate input
        Category category = Category.fromTitle(cat);
        if (category == null) {
            ModelAndView mav = showCreator();
            AlertUtil.showAlert(mav, AlertUtil.AlertType.ERROR, "error.project.categoryNotFound");
            return fillModel(mav);
        }
        if (!ID_PATTERN.matcher(pluginId).matches()) {
            ModelAndView mav = showCreator();
            AlertUtil.showAlert(mav, AlertUtil.AlertType.ERROR, "error.project.invalidPluginId");
            return fillModel(mav);
        }
        List<OrganizationsTable> orgs = userService.getOrganizationsUserCanUploadTo(userService.getCurrentUser());
        ProjectOwner ownerUser = orgs.stream().filter(org -> org.getId() == owner).map(ProjectOwner.class::cast).findAny().orElse(userService.getCurrentUser());
        // find owner
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
            AlertUtil.showAlert(mav, AlertUtil.AlertType.ERROR, ex.getMessageKey());
            return fillModel(mav);
        }

        // refresh home page
        generalDao.get().refreshHomeProjects();

        return Routes.PROJECTS_SHOW.getRedirect(project.getOwnerName(), project.getSlug());
    }

    @Secured("ROLE_USER")
    @PostMapping("/invite/{id}/{status}")
    @ResponseStatus(HttpStatus.OK)
    public void setInviteStatus(@PathVariable long id, @PathVariable String status) {
        UserProjectRolesTable projectRole = roleService.getUserProjectRole(id);
        if (projectRole == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
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
    @PostMapping("/invite/{id}/{status}/{behalf}")
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
        mav.addObject("projectPage", pagesSerivce.getPage(projectData.getProject().getId(), hangarConfig.pages.home.getName()));
        mav.addObject("editorOpen", false);
        pagesSerivce.fillPages(mav, projectData.getProject().getId());
        return fillModel(mav);
    }

    @GetMapping("/{author}/{slug}/discuss")
    public ModelAndView showDiscussion(@PathVariable String author, @PathVariable String slug) {
        ModelAndView mv = new ModelAndView("projects/discuss");
        ProjectData projectData = projectService.getProjectData(author, slug);
        ScopedProjectData scopedProjectData = projectService.getScopedProjectData(projectData.getProject().getId());
        mv.addObject("p", projectData);
        mv.addObject("sp", scopedProjectData);
        return fillModel(mv);
    }

    @Secured("ROLE_USER")
    @PostMapping("/{author}/{slug}/discuss/reply")
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
        return Routes.PROJECTS_SHOW.getRedirect(author, slug); // TODO flashing
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
    @GetMapping("/{author}/{slug}/manage")
    public ModelAndView showSettings(@PathVariable String author, @PathVariable String slug) {
        ModelAndView mav = new ModelAndView("projects/settings");
        ProjectData projectData = projectService.getProjectData(author, slug);
        mav.addObject("p", projectData);
        ScopedProjectData scopedProjectData = projectService.getScopedProjectData(projectData.getProject().getId());
        mav.addObject("sp", scopedProjectData);
        mav.addObject("iconUrl", templateHelper.projectAvatarUrl(projectData.getProject()));
        mav.addObject("deploymentKey", apiKeyService.getProjectKeys(projectData.getProject().getId()).stream().findFirst().orElse(null));
        return fillModel(mav);
    }

    @Secured("ROLE_USER")
    @PostMapping(value = "/{author}/{slug}/manage/delete", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public RedirectView softDelete(@PathVariable String author, @PathVariable String slug, @RequestParam(required = false) String comment, RedirectAttributes ra) {
        ProjectData projectData = projectService.getProjectData(author, slug);
        Visibility oldVisibility = projectData.getVisibility();

        userActionLogService.project(request, LoggedActionType.PROJECT_VISIBILITY_CHANGE.with(ProjectContext.of(projectData.getProject().getId())), Visibility.SOFTDELETE.getName(), oldVisibility.getName());
        projectFactory.softDeleteProject(projectData, comment);
        AlertUtil.showAlert(ra, AlertUtil.AlertType.SUCCESS, "project.deleted", projectData.getProject().getName());
        return new RedirectView(Routes.getRouteUrlOf("showHome"));
    }

    @Secured("ROLE_USER")
    @PostMapping("/{author}/{slug}/manage/hardDelete")
    public RedirectView delete(@PathVariable String author, @PathVariable String slug, RedirectAttributes ra) {
        ProjectData projectData = projectService.getProjectData(author, slug);
        projectFactory.hardDeleteProject(projectData);
        userActionLogService.project(request, LoggedActionType.PROJECT_VISIBILITY_CHANGE.with(ProjectContext.of(projectData.getProject().getId())), "deleted", projectData.getVisibility().getName());
        AlertUtil.showAlert(ra, AlertUtil.AlertType.SUCCESS, "project.deleted", projectData.getProject().getName());
        return new RedirectView(Routes.getRouteUrlOf("showHome"));
    }

    @Secured("ROLE_USER")
    @ProjectPermission(NamedPermission.MANAGE_SUBJECT_MEMBERS)
    @PostMapping("/{author}/{slug}/manage/members/remove")
    public ModelAndView removeMember(@PathVariable String author, @PathVariable String slug, @RequestParam String username) {
        ProjectData projectData = projectService.getProjectData(author, slug);
        UserData user = userService.getUserData(username);
        if (user == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        if (roleService.removeMember(projectData.getProject(), user.getUser().getId()) != 0) {
            userActionLogService.project(
                    request,
                    LoggedActionType.PROJECT_MEMBER_REMOVED.with(ProjectContext.of(projectData.getProject().getId())),
                    user.getUser().getName() + " is not a member of " + projectData.getNamespace(),
                    user.getUser().getName() + " is a member of " + projectData.getNamespace());
        }
        return Routes.PROJECTS_SHOW_SETTINGS.getRedirect(author, slug);
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
            AlertUtil.showAlert(mav, AlertUtil.AlertType.ERROR, "error.nameUnavailable");
            AlertUtil.showAlert(mav, AlertUtil.AlertType.ERROR, e.getMessageKey());
            return mav;
        }

        projectData.getProject().setName(compactNewName);
        projectData.getProject().setSlug(StringUtils.slugify(compactNewName));
        projectDao.get().update(projectData.getProject());
        userActionLogService.project(request, LoggedActionType.PROJECT_RENAMED.with(ProjectContext.of(projectData.getProject().getId())), author + "/" + compactNewName, author + "/" + oldName);
        return new RedirectView(Routes.getRouteUrlOf("projects.show", author, newName));
    }

    @Secured("ROLE_USER")
    @PostMapping(value = "/{author}/{slug}/manage/save", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ModelAndView save(@PathVariable String author,
                             @PathVariable String slug,
                             @RequestParam Category category,
                             @RequestParam(required = false) String keywords,
                             @RequestParam(required = false) String issues,
                             @RequestParam(required = false) String source,
                             @RequestParam(value = "license-name", required = false) String licenseName,
                             @RequestParam(value = "license-url", required = false) String licenseUrl,
                             @RequestParam("forum-sync") boolean forumSync,
                             @RequestParam String description,
                             @RequestParam("update-icon") boolean updateIcon,
                             @RequestParam(required = false) List<Long> users,
                             @RequestParam(required = false) List<Role> roles,
                             @RequestParam(required = false) List<String> userUps,
                             @RequestParam(required = false) List<Role> roleUps) {

        Set<String> keywordSet = keywords != null ? Set.of(keywords.split(" ")) : Set.of();
        if (keywordSet.size() > hangarConfig.getProjects().getMaxKeywords()) {
            ModelAndView mav = showSettings(author, slug);
            AlertUtil.showAlert(mav, AlertUtil.AlertType.ERROR, "error.project.maxKeywords", hangarConfig.getProjects().getMaxKeywords());
            return mav;
        }

        ProjectsTable projectsTable = projectService.getProjectData(author, slug).getProject();
        projectsTable.setCategory(category);
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

        // TODO perhaps bulk notification insert?
        if (users != null && roles != null) {
            for (int i = 0; i < users.size(); i++) {
                roleService.addRole(projectsTable, users.get(i), roles.get(i), false);
                notificationService.sendNotification(users.get(i), projectsTable.getOwnerId(), NotificationType.PROJECT_INVITE, new String[]{"notification.project.invite", roles.get(i).getTitle(), projectsTable.getName()});
            }
        }

        if (userUps != null && roleUps != null) {
            Map<String, UsersTable> usersToUpdate = userService.getUsers(userUps).stream().collect(Collectors.toMap(UsersTable::getName, user -> user));
            if (usersToUpdate.size() != roleUps.size())
                throw new RuntimeException("Mismatching userUps & roleUps size");
            for (int i = 0; i < userUps.size(); i++) {
                roleService.updateRole(projectsTable, usersToUpdate.get(userUps.get(i)).getId(), roleUps.get(i));
            }
        }


        userActionLogService.project(request, LoggedActionType.PROJECT_SETTINGS_CHANGED.with(ProjectContext.of(projectsTable.getId())), "", "");

        return Routes.PROJECTS_SHOW.getRedirect(author, slug);
    }

    @Secured("ROLE_USER")
    @GetMapping("/{author}/{slug}/manage/sendforapproval")
    public ModelAndView sendForApproval(@PathVariable String author, @PathVariable String slug) {
        ProjectData projectData = projectService.getProjectData(author, slug);
        if (projectData.getVisibility() == Visibility.NEEDSCHANGES) {
            projectService.changeVisibility(projectData.getProject(), Visibility.NEEDSAPPROVAL, "");
            userActionLogService.project(request, LoggedActionType.PROJECT_VISIBILITY_CHANGE.with(ProjectContext.of(projectData.getProject().getId())), Visibility.NEEDSAPPROVAL.getName(), Visibility.NEEDSCHANGES.getName());
        }
        return Routes.PROJECTS_SHOW.getRedirect(author, slug);
    }

    @GlobalPermission(NamedPermission.MOD_NOTES_AND_FLAGS)
    @Secured("ROLE_USER")
    @GetMapping("/{author}/{slug}/notes")
    public ModelAndView showNotes(@PathVariable String author, @PathVariable String slug) {
        ModelAndView mv = new ModelAndView("projects/admin/notes");
        ProjectData projectData = projectService.getProjectData(author, slug);

        List<Note> notes = new ArrayList<>();
        ArrayNode messages = (ArrayNode) projectData.getProject().getNotes().getJson().get("messages");
        if (messages != null) {
            for (JsonNode message : messages) {
                Note note = new Note().message(message.get("message").asText());
                notes.add(note);
                UserData user = userService.getUserData(message.get("user").asLong());
                note.user(user.getUser().getName());
            }
        }

        mv.addObject("project", projectData.getProject());
        mv.addObject("notes", notes);
        return fillModel(mv);
    }

    @GlobalPermission(NamedPermission.MOD_NOTES_AND_FLAGS)
    @Secured("ROLE_USER")
    @PostMapping("/{author}/{slug}/notes/addmessage")
    public ModelAndView addMessage(@PathVariable String author, @PathVariable String slug, @RequestParam String content) {
        ProjectData projectData = projectService.getProjectData(author, slug);
        ArrayNode messages = projectData.getProject().getNotes().getJson().withArray("messages");
        ObjectNode note = messages.addObject();
        note.put("message", content);
        note.put("user", userService.getCurrentUser().getId());

        String json = projectData.getProject().getNotes().getJson().toString();
        projectDao.get().updateNotes(json, projectData.getProject().getId());
        return null;
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
        Visibility oldVisibility = projectData.getVisibility();
        projectService.changeVisibility(projectData.getProject(), visibility, comment);
        userActionLogService.project(request, LoggedActionType.PROJECT_VISIBILITY_CHANGE.with(ProjectContext.of(projectData.getProject().getId())), visibility.getName(), oldVisibility.getName());
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

