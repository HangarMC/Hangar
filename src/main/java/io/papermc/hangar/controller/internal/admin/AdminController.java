package io.papermc.hangar.controller.internal.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.controller.extras.pagination.annotations.ApplicableFilters;
import io.papermc.hangar.controller.extras.pagination.annotations.ConfigurePagination;
import io.papermc.hangar.controller.extras.pagination.filters.log.LogActionFilter;
import io.papermc.hangar.controller.extras.pagination.filters.log.LogPageFilter;
import io.papermc.hangar.controller.extras.pagination.filters.log.LogProjectFilter;
import io.papermc.hangar.controller.extras.pagination.filters.log.LogSubjectFilter;
import io.papermc.hangar.controller.extras.pagination.filters.log.LogUserFilter;
import io.papermc.hangar.controller.extras.pagination.filters.log.LogVersionFilter;
import io.papermc.hangar.model.api.PaginatedResult;
import io.papermc.hangar.model.api.requests.RequestPagination;
import io.papermc.hangar.model.common.NamedPermission;
import io.papermc.hangar.model.common.roles.GlobalRole;
import io.papermc.hangar.model.db.JobTable;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.model.db.roles.GlobalRoleTable;
import io.papermc.hangar.model.internal.admin.health.MissingFileCheck;
import io.papermc.hangar.model.internal.admin.health.UnhealthyProject;
import io.papermc.hangar.model.internal.api.requests.StringContent;
import io.papermc.hangar.model.internal.api.requests.admin.ChangePlatformVersionsForm;
import io.papermc.hangar.model.internal.api.responses.HealthReport;
import io.papermc.hangar.model.internal.logs.HangarLoggedAction;
import io.papermc.hangar.security.annotations.permission.PermissionRequired;
import io.papermc.hangar.security.annotations.ratelimit.RateLimit;
import io.papermc.hangar.service.internal.JobService;
import io.papermc.hangar.service.internal.PlatformService;
import io.papermc.hangar.service.internal.admin.HealthService;
import io.papermc.hangar.service.internal.admin.StatService;
import io.papermc.hangar.service.internal.perms.roles.GlobalRoleService;
import io.papermc.hangar.service.internal.users.UserService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RateLimit(path = "admin")
@RequestMapping("/api/internal/admin")
public class AdminController extends HangarComponent {

    private final PlatformService platformService;
    private final StatService statService;
    private final HealthService healthService;
    private final JobService jobService;
    private final UserService userService;
    private final ObjectMapper mapper;
    private final GlobalRoleService globalRoleService;

    @Autowired
    public AdminController(PlatformService platformService, StatService statService, HealthService healthService, JobService jobService, UserService userService, ObjectMapper mapper, GlobalRoleService globalRoleService) {
        this.platformService = platformService;
        this.statService = statService;
        this.healthService = healthService;
        this.jobService = jobService;
        this.userService = userService;
        this.mapper = mapper;
        this.globalRoleService = globalRoleService;
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
    public void setUserLock(@PathVariable UserTable user, @PathVariable boolean locked, @RequestBody @Valid StringContent comment) {
        userService.setLocked(user, locked, comment.getContent());
    }

    @ResponseBody
    @GetMapping(value = "/log", produces = MediaType.APPLICATION_JSON_VALUE)
    @PermissionRequired(NamedPermission.REVIEWER)
    @ApplicableFilters({LogActionFilter.class, LogPageFilter.class, LogProjectFilter.class, LogSubjectFilter.class, LogUserFilter.class, LogVersionFilter.class})
    // TODO add sorters
    public PaginatedResult<HangarLoggedAction> getActionLog(@NotNull @ConfigurePagination(maxLimit = 50) RequestPagination pagination) {
        return actionLogger.getLogs(pagination);
    }

    @ResponseStatus(HttpStatus.OK)
    @PermissionRequired(NamedPermission.EDIT_ALL_USER_SETTINGS)
    @PostMapping("/user/{user}/{role}")
    public void addRole(@PathVariable UserTable user, @PathVariable String role) {
        globalRoleService.addRole(new GlobalRoleTable(user.getUserId(), GlobalRole.byApiValue(role)));
    }

    @ResponseStatus(HttpStatus.OK)
    @PermissionRequired(NamedPermission.EDIT_ALL_USER_SETTINGS)
    @DeleteMapping(value = "/user/{user}/{role}")
    public void removeRole(@PathVariable UserTable user, @PathVariable String role) {
        globalRoleService.deleteRole(new GlobalRoleTable(user.getUserId(), GlobalRole.byApiValue(role)));
    }
}
