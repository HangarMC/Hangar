package io.papermc.hangar.controllerold;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vladsch.flexmark.ext.admonition.AdmonitionExtension;
import io.papermc.hangar.config.hangar.HangarConfig;
import io.papermc.hangar.controllerold.forms.UserAdminForm;
import io.papermc.hangar.controllerold.util.StatusZ;
import io.papermc.hangar.db.customtypes.LoggedActionType;
import io.papermc.hangar.db.customtypes.LoggedActionType.ProjectContext;
import io.papermc.hangar.db.customtypes.RoleCategory;
import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.daoold.PlatformVersionsDao;
import io.papermc.hangar.db.modelold.PlatformVersionsTable;
import io.papermc.hangar.db.modelold.RoleTable;
import io.papermc.hangar.db.modelold.Stats;
import io.papermc.hangar.db.modelold.UserOrganizationRolesTable;
import io.papermc.hangar.db.modelold.UserProjectRolesTable;
import io.papermc.hangar.model.Permission;
import io.papermc.hangar.model.Platform;
import io.papermc.hangar.model.Visibility;
import io.papermc.hangar.modelold.NamedPermission;
import io.papermc.hangar.modelold.Role;
import io.papermc.hangar.modelold.viewhelpers.Activity;
import io.papermc.hangar.modelold.viewhelpers.LoggedActionViewModel;
import io.papermc.hangar.modelold.viewhelpers.OrganizationData;
import io.papermc.hangar.modelold.viewhelpers.ProjectFlag;
import io.papermc.hangar.modelold.viewhelpers.ReviewQueueEntry;
import io.papermc.hangar.modelold.viewhelpers.UnhealthyProject;
import io.papermc.hangar.modelold.viewhelpers.UserData;
import io.papermc.hangar.security.annotations.GlobalPermission;
import io.papermc.hangar.serviceold.JobService;
import io.papermc.hangar.serviceold.OrgService;
import io.papermc.hangar.serviceold.RoleService;
import io.papermc.hangar.serviceold.SitemapService;
import io.papermc.hangar.serviceold.StatsService;
import io.papermc.hangar.serviceold.UserActionLogService;
import io.papermc.hangar.serviceold.UserService;
import io.papermc.hangar.serviceold.VersionService;
import io.papermc.hangar.serviceold.project.FlagService;
import io.papermc.hangar.serviceold.project.ProjectService;
import io.papermc.hangar.util.AlertUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Controller
public class ApplicationController extends HangarController {

    private final HangarDao<PlatformVersionsDao> platformVersionsDao;
    private final UserService userService;
    private final ProjectService projectService;
    private final FlagService flagService;
    private final OrgService orgService;
    private final UserActionLogService userActionLogService;
    private final VersionService versionService;
    private final JobService jobService;
    private final SitemapService sitemapService;
    private final StatsService statsService;
    private final RoleService roleService;
    private final StatusZ statusZ;
    private final ObjectMapper mapper;
    private final HangarConfig hangarConfig;

    private final HttpServletRequest request;
    private final Supplier<UserData> userData;

    private final io.papermc.hangar.service.internal.RoleService roleServiceNew;

    @Autowired
    public ApplicationController(HangarDao<PlatformVersionsDao> platformVersionsDao, UserService userService, ProjectService projectService, OrgService orgService, VersionService versionService, FlagService flagService, UserActionLogService userActionLogService, JobService jobService, SitemapService sitemapService, StatsService statsService, RoleService roleService, StatusZ statusZ, ObjectMapper mapper, HangarConfig hangarConfig, HttpServletRequest request, Supplier<UserData> userData, io.papermc.hangar.service.internal.RoleService roleServiceNew) {
        this.platformVersionsDao = platformVersionsDao;
        this.userService = userService;
        this.projectService = projectService;
        this.orgService = orgService;
        this.flagService = flagService;
        this.userActionLogService = userActionLogService;
        this.versionService = versionService;
        this.jobService = jobService;
        this.sitemapService = sitemapService;
        this.roleService = roleService;
        this.statusZ = statusZ;
        this.mapper = mapper;
        this.hangarConfig = hangarConfig;
        this.request = request;
        this.statsService = statsService;
        this.userData = userData;
        this.roleServiceNew = roleServiceNew;
    }

    @GetMapping("/")
    public ModelAndView showHome(ModelMap modelMap) {
        ModelAndView mav = new ModelAndView("home");
        AlertUtil.transferAlerts(mav, modelMap);
        return fillModel(mav);
    }

    @RequestMapping("/statusz")
    public ResponseEntity<ObjectNode> showStatusZ() {
        return ResponseEntity.ok(statusZ.getStatus());
    }

    @GlobalPermission(NamedPermission.REVIEWER)
    @Secured("ROLE_USER")
    @GetMapping("/admin/activities/{user}")
    public ModelAndView showActivities(@PathVariable String user) {
        ModelAndView mv = new ModelAndView("users/admin/activity");
        mv.addObject("username", user);
        List<Activity> activities = new ArrayList<>();
        activities.addAll(userService.getFlagActivity(user));
        activities.addAll(userService.getReviewActivity(user));
        mv.addObject("activities", activities);
        return fillModel(mv);
    }

    @GlobalPermission(NamedPermission.REVIEWER)
    @Secured("ROLE_USER")
    @GetMapping("/admin/approval/projects")
    public Object showProjectVisibility() {
        ModelAndView mv = new ModelAndView("users/admin/visibility");
        mv.addObject("needsApproval", projectService.getProjectsNeedingApproval());
        mv.addObject("waitingProjects", projectService.getProjectsWaitingForChanges());
        return fillModel(mv);
    }

    @GlobalPermission(NamedPermission.REVIEWER)
    @Secured("ROLE_USER")
    @GetMapping("/admin/approval/versions")
    public ModelAndView showQueue() {
        ModelAndView mv = new ModelAndView("users/admin/queue");
        List<ReviewQueueEntry> reviewQueueEntries = new ArrayList<>();
        List<ReviewQueueEntry> notStartedQueueEntries = new ArrayList<>();
        versionService.getReviewQueue().forEach(entry -> (entry.hasReviewer() ? reviewQueueEntries : notStartedQueueEntries).add(entry));
        mv.addObject("underReview", reviewQueueEntries);
        mv.addObject("versions", notStartedQueueEntries);
        return fillModel(mv);
    }

    @GlobalPermission(NamedPermission.MOD_NOTES_AND_FLAGS)
    @Secured("ROLE_USER")
    @GetMapping("/admin/flags")
    public ModelAndView showFlags() {
        ModelAndView mav = new ModelAndView("users/admin/flags");
        mav.addObject("flags", flagService.getAllProjectFlags());
        return fillModel(mav);
    }

    @GlobalPermission(NamedPermission.MOD_NOTES_AND_FLAGS)
    @Secured("ROLE_USER")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/admin/flags/{id}/resolve/{resolved}")
    public void setFlagResolved(@PathVariable long id, @PathVariable boolean resolved) {
        ProjectFlag flag = flagService.getProjectFlag(id);
        if (flag == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        if (flag.getFlag().getIsResolved() == resolved) return; // No change

        flagService.markAsResolved(id, resolved);
        String userName = getCurrentUser().getName();
        userActionLogService.project(request, LoggedActionType.PROJECT_FLAG_RESOLVED.with(ProjectContext.of(flag.getFlag().getProjectId())), "Flag resovled by " + userName, "Flagged by " + flag.getReportedBy());
    }

    @GlobalPermission(NamedPermission.VIEW_HEALTH)
    @Secured("ROLE_USER")
    @GetMapping("/admin/health")
    public ModelAndView showHealth() {
        ModelAndView mav = new ModelAndView("users/admin/health");
        List<UnhealthyProject> unhealthyProjects = projectService.getUnhealthyProjects();
        mav.addObject("noTopicProjects", unhealthyProjects.stream().filter(p -> p.getTopicId() == null || p.getPostId() == null).collect(Collectors.toList()));
        mav.addObject("staleProjects", unhealthyProjects);
        mav.addObject("notPublicProjects", unhealthyProjects.stream().filter(p -> p.getVisibility() != Visibility.PUBLIC).collect(Collectors.toList()));
        mav.addObject("missingFileProjects", projectService.getPluginsWithMissingFiles());
        mav.addObject("erroredJobs", jobService.getErroredJobs());
        return fillModel(mav);
    }

    @GlobalPermission(NamedPermission.VIEW_LOGS)
    @Secured("ROLE_USER")
    @GetMapping("/admin/log")
    public ModelAndView showLog(@RequestParam(required = false) Integer oPage,
                                @RequestParam(required = false) String userFilter,
                                @RequestParam(required = false) String projectFilter,
                                @RequestParam(required = false) String versionFilter,
                                @RequestParam(required = false) String pageFilter,
                                @RequestParam(required = false) String actionFilter,
                                @RequestParam(required = false) String subjectFilter) {
        ModelAndView mv = new ModelAndView("users/admin/log");
        int pageSize = 50;
        int page = oPage != null ? oPage : 1;
        int offset = (page - 1) * pageSize;
        List<LoggedActionViewModel<?>> log = userActionLogService.getLog(oPage, userFilter, projectFilter, versionFilter, pageFilter, actionFilter, subjectFilter);
        mv.addObject("actions", log);
        mv.addObject("limit", pageSize);
        mv.addObject("offset", offset);
        mv.addObject("page", page);
        mv.addObject("size", 10); //TODO sum of table sizes of all LoggedAction tables (I think)
        mv.addObject("userFilter", userFilter);
        //TODO filter slug and author, not just slug (temp fix was to just concatenate the owner and slug together)
        mv.addObject("projectFilter", projectFilter);
        mv.addObject("versionFilter", versionFilter);
        mv.addObject("pageFilter", pageFilter);
        mv.addObject("actionFilter", actionFilter);
        mv.addObject("subjectFilter", subjectFilter);
        mv.addObject("canViewIP", userService.getHeaderData().getGlobalPermission().has(Permission.ViewIp));
        return fillModel(mv);
    }

    @GlobalPermission(NamedPermission.VIEW_STATS)
    @Secured("ROLE_USER")
    @GetMapping("/admin/stats")
    public ModelAndView showStats(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from, @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        ModelAndView mav = new ModelAndView("users/admin/stats");
        if (from == null) {
            from = LocalDate.now().minus(30, ChronoUnit.DAYS);
        }
        if (to == null) {
            to = LocalDate.now();
        }
        if (to.isBefore(from)) {
            to = from;
        }
        List<Stats> stats = statsService.getStats(from, to);
        mav.addObject("fromDate", from.toString());
        mav.addObject("toDate", to.toString());
        mav.addObject("days", statsService.getStatDays(stats));
        mav.addObject("reviewData", statsService.getReviewStats(stats));
        mav.addObject("uploadData", statsService.getUploadStats(stats));
        mav.addObject("totalDownloadData", statsService.getTotalDownloadStats(stats));
        mav.addObject("unsafeDownloadData", statsService.getUnsafeDownloadsStats(stats));
        mav.addObject("openFlagsData", statsService.getFlagsOpenedStats(stats));
        mav.addObject("closedFlagsData", statsService.getFlagsClosedStats(stats));

        return fillModel(mav);
    }

    @GlobalPermission(NamedPermission.MANUAL_VALUE_CHANGES)
    @Secured("ROLE_USER")
    @GetMapping("/admin/versions")
    public ModelAndView showPlatformVersions() {
        ModelAndView mav = new ModelAndView("users/admin/platformVersions");
        Map<Platform, List<String>> versions = platformVersionsDao.get().getVersions();
        for (Platform p : Platform.VALUES) {
            versions.putIfAbsent(p, new ArrayList<>());
        }
        mav.addObject("platformVersions", mapper.valueToTree(versions));
        return fillModel(mav);
    }

    @SuppressWarnings("unchecked")
    @GlobalPermission(NamedPermission.MANUAL_VALUE_CHANGES)
    @Secured("ROLE_USER")
    @PostMapping("/admin/versions/")
    @ResponseStatus(HttpStatus.OK)
    public void updatePlatformVersions(@RequestBody ObjectNode object) {
        Map<String, List<String>> additions;
        Map<String, List<String>> removals;
        try {
            additions = mapper.treeToValue(object.get("additions"), (Class<Map<String, List<String>>>) (Class) Map.class);
            removals = mapper.treeToValue(object.get("removals"), (Class<Map<String, List<String>>>) (Class) Map.class);
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad formatting", e);
        }
        if (additions == null || removals == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad formatting");
        }
        additions.forEach((platform, versions) -> platformVersionsDao.get().insert(versions.stream().map(v -> new PlatformVersionsTable(Platform.valueOf(platform), v)).collect(Collectors.toList())));
        removals.forEach((platform, versions) -> platformVersionsDao.get().delete(versions, Platform.valueOf(platform).ordinal()));
    }

    @GlobalPermission(NamedPermission.EDIT_ALL_USER_SETTINGS)
    @Secured("ROLE_USER")
    @GetMapping("/admin/user/{user}")
    public ModelAndView userAdmin(@PathVariable String user) {
        ModelAndView mav = new ModelAndView("users/admin/userAdmin");
        UserData userData = userService.getUserData(user);
        mav.addObject("u", userData);
        OrganizationData organizationData = orgService.getOrganizationData(userData.getUser());
        mav.addObject("orga", organizationData);
        mav.addObject("userProjectRoles", projectService.getProjectsAndRoles(userData.getUser().getId()));
        return fillModel(mav);
    }


    private static final String ORG_ROLE = "orgRole";
    private static final String MEMBER_ROLE = "memberRole";
    private static final String PROJECT_ROLE = "projectRole";
    @GlobalPermission(NamedPermission.EDIT_ALL_USER_SETTINGS)
    @Secured("ROLE_USER")
    @PostMapping(value = "/admin/user/{user}/update", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public void updateUser(@PathVariable("user") String userName, @RequestBody UserAdminForm userAdminForm) {
        UserData user = userData.get();
        switch (userAdminForm.getThing()) {
            case ORG_ROLE: {
                if (user.isOrga()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
                }
                UserOrganizationRolesTable orgRole = roleService.getUserOrgRole(userAdminForm.getData().get("id").asLong());
                break;
            }
            case MEMBER_ROLE: {
                if (!user.isOrga()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
                }
                UserOrganizationRolesTable orgRole = roleService.getUserOrgRole(userAdminForm.getData().get("id").asLong());
                updateRoleTable(userAdminForm, orgRole, RoleCategory.ORGANIZATION, Role.ORGANIZATION_OWNER);
                break;
            }
            case PROJECT_ROLE: {
                UserProjectRolesTable projectRole = roleService.getUserProjectRole(userAdminForm.getData().get("id").asLong());
                updateRoleTable(userAdminForm, projectRole, RoleCategory.PROJECT, Role.PROJECT_OWNER);
                break;
            }
            default:
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    private static final String SET_ROLE = "setRole";
    private static final String SET_ACCEPTED = "setAccepted";
    private static final String DELETE_ROLE = "deleteRole";
    private <R extends RoleTable> void updateRoleTable(UserAdminForm userAdminForm, R userRole, RoleCategory allowedCategory, Role ownerType) {
        switch (userAdminForm.getAction()) {
            case SET_ROLE:
                Role role = Role.fromId(userAdminForm.getData().get("role").asLong());
                if (role == null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
                }
                else if (role == ownerType) {
                    // TODO transfer owner
                }
                else if (role.getCategory() == allowedCategory && role.isAssignable()) {
                    userRole.setRoleType(role.getValue());
                    roleService.updateRole(userRole);
                } else {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
                }
                break;
            case SET_ACCEPTED:
                userRole.setIsAccepted(true);
                roleService.updateRole(userRole);
                break;
            case DELETE_ROLE:
                if (userRole.getRole().isAssignable()) {
                    roleService.removeRole(userRole);
                }
                break;
            default:
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/api") // TODO move to Apiv1Controller maybe
    public ModelAndView swagger() {
        return fillModel(new ModelAndView("swagger"));
    }

    @GetMapping(value = "/favicon.ico", produces = "images/x-icon")
    @ResponseBody
    public ClassPathResource faviconRedirect() {
        return new ClassPathResource("public/images/favicon/favicon.ico");
    }

    @GetMapping(value = "/global-sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public String globalSitemap() {
        return sitemapService.getGlobalSitemap();
    }

    @GetMapping("/linkout")
    public ModelAndView linkOut(@RequestParam(defaultValue = "") String remoteUrl) {
        ModelAndView view = new ModelAndView("linkout");
        view.addObject("remoteUrl", remoteUrl);
        return fillModel(view);
    }

    @GetMapping(value = "/robots.txt", produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public Object robots() {
        if (hangarConfig.isUseWebpack()) {
            request.setAttribute(View.RESPONSE_STATUS_ATTRIBUTE, HttpStatus.MOVED_PERMANENTLY);
            return new ModelAndView("redirect:http://localhost:8081/robots.txt");
        } else {
            return new ClassPathResource("public/robots.txt");
        }
    }

    @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public String sitemapIndex() {
        return sitemapService.getSitemap();
    }

    @GetMapping(value = "/assets-ext/css/admonition.css", produces = "text/css")
    @ResponseBody
    public String admonitionCss() {
        return AdmonitionExtension.getDefaultCSS();
    }

    @GetMapping(value = "/assets-ext/js/admonition.js", produces = "text/javascript")
    @ResponseBody
    public String admonitionJs() {
        return AdmonitionExtension.getDefaultScript().replace("(() => {", "window.admonition = () => {").replace("})();", "};");
    }
}
