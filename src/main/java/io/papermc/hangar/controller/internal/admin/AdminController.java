package io.papermc.hangar.controller.internal.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.controller.extras.resolvers.NoCache;
import io.papermc.hangar.model.api.PaginatedResult;
import io.papermc.hangar.model.api.Pagination;
import io.papermc.hangar.model.common.NamedPermission;
import io.papermc.hangar.model.db.JobTable;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.model.internal.admin.health.MissingFileCheck;
import io.papermc.hangar.model.internal.admin.health.UnhealthyProject;
import io.papermc.hangar.model.internal.api.requests.StringContent;
import io.papermc.hangar.model.internal.api.requests.admin.ChangePlatformVersionsForm;
import io.papermc.hangar.model.internal.api.responses.HealthReport;
import io.papermc.hangar.model.internal.logs.HangarLoggedAction;
import io.papermc.hangar.security.annotations.permission.PermissionRequired;
import io.papermc.hangar.service.internal.JobService;
import io.papermc.hangar.service.internal.PlatformService;
import io.papermc.hangar.service.internal.admin.HealthService;
import io.papermc.hangar.service.internal.admin.StatService;
import io.papermc.hangar.service.internal.users.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/api/internal/admin")
public class AdminController extends HangarComponent {

    private final PlatformService platformService;
    private final StatService statService;
    private final HealthService healthService;
    private final JobService jobService;
    private final UserService userService;
    private final ObjectMapper mapper;

    @Autowired
    public AdminController(PlatformService platformService, StatService statService, HealthService healthService, JobService jobService, UserService userService, ObjectMapper mapper) {
        this.platformService = platformService;
        this.statService = statService;
        this.healthService = healthService;
        this.jobService = jobService;
        this.userService = userService;
        this.mapper = mapper;
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping(path = "/platformVersions", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PermissionRequired(NamedPermission.MANUAL_VALUE_CHANGES)
    public void changePlatformVersions(@RequestBody @Valid ChangePlatformVersionsForm form) {
        platformService.updatePlatformVersions(form);
    }

    @ResponseBody
    @PermissionRequired(NamedPermission.VIEW_STATS)
    @GetMapping(path = "/stats", produces = MediaType.APPLICATION_JSON_VALUE)
    public ArrayNode getStats(@RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate from, @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate to) {
        if (from == null) {
            from = LocalDate.now().minusDays(30);
        }
        if (to == null) {
            to = LocalDate.now();
        }
        if (to.isBefore(from)) {
            to = from;
        }
        return mapper.valueToTree(statService.getStats(from, to));
    }

    @ResponseBody
    @PermissionRequired(NamedPermission.VIEW_HEALTH)
    @GetMapping(path = "/health", produces = MediaType.APPLICATION_JSON_VALUE)
    public HealthReport getHealthReport() {
        List<UnhealthyProject> noTopicProjects = healthService.getProjectsWithoutTopic();
        List<UnhealthyProject> staleProjects = healthService.getStaleProjects();
        List<UnhealthyProject> nonPublicProjects = healthService.getNonPublicProjects();
        List<MissingFileCheck> missingFiles = healthService.getVersionsWithMissingFiles();
        List<JobTable> erroredJobs = jobService.getErroredJobs();
        return new HealthReport(noTopicProjects, staleProjects, nonPublicProjects, missingFiles, erroredJobs);
    }

    @ResponseStatus(HttpStatus.OK)
    @PermissionRequired(NamedPermission.IS_STAFF)
    @PostMapping(value = "/lock-user/{user}/{locked}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void setUserLock(@PathVariable @NoCache UserTable user, @PathVariable boolean locked, @RequestBody @Valid StringContent comment) {
        userService.setLocked(user, locked, comment.getContent());
    }

    // TODO filters/pagination
    @ResponseBody
    @GetMapping(value = "/log", produces = MediaType.APPLICATION_JSON_VALUE)
    @PermissionRequired(NamedPermission.REVIEWER)
    public PaginatedResult<HangarLoggedAction> getActionLog() {
        List<HangarLoggedAction> log = userActionLogService.getLog(0, null, null, null, null, null, null);
        return new PaginatedResult<>(new Pagination(25, 0, (long) log.size()), log);
    }
}
