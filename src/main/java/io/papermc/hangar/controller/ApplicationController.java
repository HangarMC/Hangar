package io.papermc.hangar.controller;

import io.papermc.hangar.db.customtypes.LoggedActionType;
import io.papermc.hangar.db.customtypes.LoggedActionType.ProjectContext;
import io.papermc.hangar.db.model.Stats;
import io.papermc.hangar.model.NamedPermission;
import io.papermc.hangar.model.Permission;
import io.papermc.hangar.model.Visibility;
import io.papermc.hangar.model.viewhelpers.Activity;
import io.papermc.hangar.model.viewhelpers.LoggedActionViewModel;
import io.papermc.hangar.model.viewhelpers.OrganizationData;
import io.papermc.hangar.model.viewhelpers.ProjectFlag;
import io.papermc.hangar.model.viewhelpers.ReviewQueueEntry;
import io.papermc.hangar.model.viewhelpers.UnhealthyProject;
import io.papermc.hangar.model.viewhelpers.UserData;
import io.papermc.hangar.security.annotations.GlobalPermission;
import io.papermc.hangar.service.JobService;
import io.papermc.hangar.service.OrgService;
import io.papermc.hangar.service.SitemapService;
import io.papermc.hangar.service.StatsService;
import io.papermc.hangar.service.UserActionLogService;
import io.papermc.hangar.service.UserService;
import io.papermc.hangar.service.VersionService;
import io.papermc.hangar.service.project.FlagService;
import io.papermc.hangar.service.project.ProjectService;
import io.papermc.hangar.util.AlertUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ApplicationController extends HangarController {

    private final UserService userService;
    private final ProjectService projectService;
    private final FlagService flagService;
    private final OrgService orgService;
    private final UserActionLogService userActionLogService;
    private final VersionService versionService;
    private final JobService jobService;
    private final SitemapService sitemapService;
    private final StatsService statsService;

    private final HttpServletRequest request;

    @Autowired
    public ApplicationController(UserService userService, ProjectService projectService, OrgService orgService, VersionService versionService, FlagService flagService, UserActionLogService userActionLogService, JobService jobService, SitemapService sitemapService, StatsService statsService, HttpServletRequest request) {
        this.userService = userService;
        this.projectService = projectService;
        this.orgService = orgService;
        this.flagService = flagService;
        this.userActionLogService = userActionLogService;
        this.versionService = versionService;
        this.jobService = jobService;
        this.sitemapService = sitemapService;
        this.request = request;
        this.statsService = statsService;
    }

    @GetMapping("/")
    public ModelAndView showHome(ModelMap modelMap) {
        ModelAndView mav = new ModelAndView("home");
        AlertUtil.transferAlerts(mav, modelMap);
        return fillModel(mav);
    }

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
        String userName = userService.getCurrentUser().getName();
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
        mv.addObject("projectFilter", projectFilter);
        mv.addObject("versionFilter", versionFilter);
        mv.addObject("pageFilter", pageFilter);
        mv.addObject("actionFilter", actionFilter);
        mv.addObject("subjectFilter", subjectFilter);
        mv.addObject("canViewIP", userService.getHeaderData().getGlobalPermission().has(Permission.ViewIp));
        return fillModel(mv);
    }

    @Secured("ROLE_USER")
    @GetMapping("/admin/stats")
    public ModelAndView showStats(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from, @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        ModelAndView mav = new ModelAndView("users/admin/stats");
        if(from == null){
            from = LocalDate.now().minus(30, ChronoUnit.DAYS);
        }
        if(to == null){
            to = LocalDate.now();
        }
        if(to.isBefore(from)){
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

    @Secured("ROLE_USER")
    @PostMapping("/admin/user/{user}/update")
    public Object updateUser(@PathVariable Object user) {
        return null; // TODO implement updateUser request controller
    }

    @GetMapping("/api") // TODO move to Apiv1Controller maybe
    public ModelAndView swagger() {
        return fillModel(new ModelAndView("swagger"));
    }

    @GetMapping(value = "/favicon.ico", produces = "images/x-icon")
    @ResponseBody
    public ClassPathResource faviconRedirect() {
        return new ClassPathResource("public/images/favicon.ico");
    }

    @GetMapping(value = "/global-sitemap.xml", produces =  MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public String globalSitemap() {
        return sitemapService.getGlobalSitemap();
    }

    @GetMapping(value = "/javascriptRoutes", produces = "text/javascript")
    @ResponseBody
    public String javaScriptRoutes() {
        // yeah, dont even ask wtf is happening here, I dont have an answer
        return "var jsRoutes = {}; (function(_root){\n" +
               "var _nS = function(c,f,b){var e=c.split(f||\".\"),g=b||_root,d,a;for(d=0,a=e.length;d<a;d++){g=g[e[d]]=g[e[d]]||{}}return g}\n" +
               "var _qS = function(items){var qs = ''; for(var i=0;i<items.length;i++) {if(items[i]) qs += (qs ? '&' : '') + items[i]}; return qs ? ('?' + qs) : ''}\n" +
               "var _s = function(p,s){return p+((s===true||(s&&s.secure))?'s':'')+'://'}\n" +
               "var _wA = function(r){return {ajax:function(c){c=c||{};c.url=r.url;c.type=r.method;return jQuery.ajax(c)}, method:r.method,type:r.method,url:r.url,absoluteURL: function(s){return _s('http',s)+'localhost:8080'+r.url},webSocketURL: function(s){return _s('ws',s)+'localhost:9000'+r.url}}}\n" +
               "_nS('controllers.project.Projects'); _root['controllers']['project']['Projects']['show'] = \n" +
               "        function(author0,slug1) {\n" +
               "          return _wA({method:\"GET\", url:\"/\" + encodeURIComponent((function(k,v) {return v})(\"author\", author0)) + \"/\" + encodeURIComponent((function(k,v) {return v})(\"slug\", slug1))})\n" +
               "        }\n" +
               "      ;\n" +
               "_nS('controllers.project.Versions'); _root['controllers']['project']['Versions']['show'] = \n" +
               "        function(author0,slug1,version2) {\n" +
               "          return _wA({method:\"GET\", url:\"/\" + encodeURIComponent((function(k,v) {return v})(\"author\", author0)) + \"/\" + encodeURIComponent((function(k,v) {return v})(\"slug\", slug1)) + \"/versions/\" + encodeURIComponent((function(k,v) {return v})(\"version\", version2))})\n" +
               "        }\n" +
               "      ;\n" +
               "_nS('controllers.project.Versions'); _root['controllers']['project']['Versions']['showCreator'] = \n" +
               "        function(author0,slug1) {\n" +
               "          return _wA({method:\"GET\", url:\"/\" + encodeURIComponent((function(k,v) {return v})(\"author\", author0)) + \"/\" + encodeURIComponent((function(k,v) {return v})(\"slug\", slug1)) + \"/versions/new\"})\n" +
               "        }\n" +
               "      ;\n" +
               "_nS('controllers.Users'); _root['controllers']['Users']['showProjects'] = \n" +
               "        function(user0) {\n" +
               "          return _wA({method:\"GET\", url:\"/\" + encodeURIComponent((function(k,v) {return v})(\"user\", user0))})\n" +
               "        }\n" +
               "      ;\n" +
               "})(jsRoutes)"; // TODO implement javaScriptRoutes request controller
    }

    @GetMapping("/linkout")
    public ModelAndView linkOut(@RequestParam(defaultValue = "") String remoteUrl) {
        ModelAndView view = new ModelAndView("linkout");
        view.addObject("remoteUrl", remoteUrl);
        return fillModel(view);
    }

    @Secured("ROLE_USER")
    @GetMapping("/pantopticon/actor-count")
    public Object actorCount(@RequestParam Object timeoutMs) {
        return null; // TODO implement actorCount request controller
    }

    @Secured("ROLE_USER")
    @GetMapping("/pantopticon/actor-tree")
    public Object actorTree(@RequestParam Object timeoutMs) {
        return null; // TODO implement actorTree request controller
    }

    @GetMapping(value = "/robots.txt", produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public Object robots() {
        return new ClassPathResource("public/robots.txt");
    }

    @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public String sitemapIndex() {
        return sitemapService.getSitemap();
    }

}
