package me.minidigger.hangar.controller;

import me.minidigger.hangar.db.customtypes.LoggedActionType;
import me.minidigger.hangar.db.customtypes.LoggedActionType.ProjectContext;
import me.minidigger.hangar.model.NamedPermission;
import me.minidigger.hangar.model.Permission;
import me.minidigger.hangar.model.Visibility;
import me.minidigger.hangar.model.generated.ProjectNamespace;
import me.minidigger.hangar.model.generated.Version;
import me.minidigger.hangar.model.viewhelpers.Activity;
import me.minidigger.hangar.model.viewhelpers.FlagActivity;
import me.minidigger.hangar.model.viewhelpers.LoggedActionViewModel;
import me.minidigger.hangar.model.viewhelpers.ProjectFlag;
import me.minidigger.hangar.model.viewhelpers.Review;
import me.minidigger.hangar.model.viewhelpers.ReviewActivity;
import me.minidigger.hangar.model.viewhelpers.ReviewQueueEntry;
import me.minidigger.hangar.model.viewhelpers.UnhealthyProject;
import me.minidigger.hangar.model.viewhelpers.UserData;
import me.minidigger.hangar.security.annotations.GlobalPermission;
import me.minidigger.hangar.service.JobService;
import me.minidigger.hangar.service.UserActionLogService;
import me.minidigger.hangar.service.UserService;
import me.minidigger.hangar.service.VersionService;
import me.minidigger.hangar.service.project.FlagService;
import me.minidigger.hangar.service.project.ProjectService;
import me.minidigger.hangar.util.AlertUtil;
import me.minidigger.hangar.util.AlertUtil.AlertType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ApplicationController extends HangarController {

    private final UserService userService;
    private final ProjectService projectService;
    private final FlagService flagService;
    private final UserActionLogService userActionLogService;
    private final VersionService versionService;
    private final JobService jobService;

    @Autowired
    public ApplicationController(UserService userService, ProjectService projectService, VersionService versionService, FlagService flagService, UserActionLogService userActionLogService, JobService jobService) {
        this.userService = userService;
        this.projectService = projectService;
        this.flagService = flagService;
        this.userActionLogService = userActionLogService;
        this.versionService = versionService;
        this.jobService = jobService;
    }

    @RequestMapping("/")
    public ModelAndView showHome(@ModelAttribute("alertType") String alertType, @ModelAttribute("alertMsg") String alertMsg) {
        ModelAndView mav = new ModelAndView("home");
        AlertType type;
        try {
            type = AlertType.valueOf(alertType);
        } catch (IllegalArgumentException e) {
            type = null;
        }
        if (type != null && alertMsg != null)
            AlertUtil.showAlert(mav, type, alertMsg);
        return fillModel(mav);
    }

    @Secured("ROLE_USER")
    @GetMapping("/admin/activities/{user}")
    public ModelAndView showActivities(@PathVariable String user) {
        ModelAndView mv = new ModelAndView("users/admin/activity");
        mv.addObject("username", user);

        List<Activity> activities = new ArrayList<>();
        activities.addAll(userService.getFlagActivity(user));
        activities.addAll(userService.getRewiewActivity(user));

        activities.add(new ReviewActivity(OffsetDateTime.now(),
                new Review(new Version(), 1L, OffsetDateTime.now(), "suks"), new ProjectNamespace().owner("Kneny").slug("Swags")));
        activities.add(new FlagActivity(OffsetDateTime.now(), new ProjectNamespace().owner("Kneny").slug("Swags")));

        mv.addObject("activities", activities);
        return mv;
    }

    @GlobalPermission(NamedPermission.REVIEWER)
    @Secured("ROLE_USER")
    @RequestMapping("/admin/approval/projects")
    public Object showProjectVisibility() {
        ModelAndView mv = new ModelAndView("users/admin/visibility");
        mv.addObject("needsApproval", projectService.getProjectsNeedingApproval());
        mv.addObject("waitingProjects", projectService.getProjectsWaitingForChanges());
        return fillModel(mv);
    }

    @Secured("ROLE_USER")
    @RequestMapping("/admin/approval/versions")
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
    public void setFlagResolved(@PathVariable long id, @PathVariable boolean resolved, HttpServletRequest request) {
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
    @RequestMapping("/admin/health")
    public ModelAndView showHealth() {
        ModelAndView mav = new ModelAndView("users/admin/health");
        List<UnhealthyProject> unhealthyProjects = projectService.getUnhealthyProjects();
        mav.addObject("noTopicProjects", unhealthyProjects.stream().filter(p -> p.getTopicId() == null || p.getPostId() == null).collect(Collectors.toList()));
        mav.addObject("staleProjects", unhealthyProjects);
        mav.addObject("notPublicProjects", unhealthyProjects.stream().filter(p -> p.getVisibility() != Visibility.PUBLIC).collect(Collectors.toList()));
        // TODO missingFiles
        mav.addObject("missingFileProjects");
        mav.addObject("erroredJobs", jobService.getErroredJobs());
        return fillModel(mav);
    }

    @Secured("ROLE_USER")
    @RequestMapping("/admin/log")
    public ModelAndView showLog(@RequestParam(required = false) Integer oPage,
                                @RequestParam(required = false) Object userFilter,
                                @RequestParam(required = false) Object projectFilter,
                                @RequestParam(required = false) Object versionFilter,
                                @RequestParam(required = false) Object pageFilter,
                                @RequestParam(required = false) Object actionFilter,
                                @RequestParam(required = false) Object subjectFilter) {
        ModelAndView mv = new ModelAndView("users/admin/log");
        int pageSize = 50;
        int page = oPage != null ? oPage : 1;
        int offset = (page - 1) * pageSize;
        List<LoggedActionViewModel> log = userActionLogService.getLog(oPage, userFilter, projectFilter, versionFilter, pageFilter, actionFilter, subjectFilter);
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
    @RequestMapping("/admin/stats")
    public ModelAndView showStats(@RequestParam(required = false) Object from, @RequestParam(required = false) Object to) {
        return fillModel(new ModelAndView("users/admin/stats")); // TODO implement showStats request controller
    }

    @GlobalPermission(NamedPermission.EDIT_ALL_USER_SETTINGS)
    @Secured("ROLE_USER")
    @RequestMapping("/admin/user/{user}")
    public ModelAndView userAdmin(@PathVariable String user) {
        ModelAndView mav = new ModelAndView("users/admin/userAdmin");
        UserData userData = userService.getUserData(user);
        mav.addObject("u", userData);
        // TODO organization
        mav.addObject("userProjectRoles", projectService.getProjectsAndRoles(userData.getUser().getId()));
        return fillModel(mav);
    }

    @Secured("ROLE_USER")
    @RequestMapping("/admin/user/{user}/update")
    public Object updateUser(@PathVariable Object user) {
        return null; // TODO implement updateUser request controller
    }

    @RequestMapping("/api")
    public ModelAndView swagger() {
        return fillModel(new ModelAndView("swagger"));
    }

    @RequestMapping("/favicon.ico")
    @ResponseBody
    public Object faviconRedirect() {
        return "no u"; // TODO implement faviconRedirect request controller
    }

    @RequestMapping("/global-sitemap.xml")
    public Object globalSitemap() {
        return null; // TODO implement globalSitemap request controller
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

    @RequestMapping("/linkout")
    public ModelAndView linkOut(@RequestParam(defaultValue = "") String remoteUrl) {
        ModelAndView view = new ModelAndView("linkout");
        view.addObject("remoteUrl", remoteUrl);
        return fillModel(view);
    }

    @Secured("ROLE_USER")
    @RequestMapping("/pantopticon/actor-count")
    public Object actorCount(@RequestParam Object timeoutMs) {
        return null; // TODO implement actorCount request controller
    }

    @Secured("ROLE_USER")
    @RequestMapping("/pantopticon/actor-tree")
    public Object actorTree(@RequestParam Object timeoutMs) {
        return null; // TODO implement actorTree request controller
    }

    @RequestMapping("/robots.txt")
    public Object robots() {
        return null; // TODO implement robots request controller
    }

    @RequestMapping("/sitemap.xml")
    public Object sitemapIndex() {
        return null; // TODO implement sitemapIndex request controller
    }

//    @RequestMapping("/{path}/")
//    public Object removeTrail(@PathVariable Object path) {
//        return null; // implement removeTrail request controller - pretty sure this one is dum
//    }
}
