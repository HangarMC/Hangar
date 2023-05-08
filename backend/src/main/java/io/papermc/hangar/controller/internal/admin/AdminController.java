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
import io.papermc.hangar.db.dao.internal.table.roles.RolesDAO;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.api.PaginatedResult;
import io.papermc.hangar.model.api.requests.RequestPagination;
import io.papermc.hangar.model.common.NamedPermission;
import io.papermc.hangar.model.common.Permission;
import io.papermc.hangar.model.common.projects.Visibility;
import io.papermc.hangar.model.common.roles.GlobalRole;
import io.papermc.hangar.model.db.JobTable;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.model.db.projects.ProjectTable;
import io.papermc.hangar.model.db.roles.GlobalRoleTable;
import io.papermc.hangar.model.internal.admin.health.MissingFileCheck;
import io.papermc.hangar.model.internal.admin.health.UnhealthyProject;
import io.papermc.hangar.model.internal.api.requests.StringContent;
import io.papermc.hangar.model.internal.api.requests.admin.ChangePlatformVersionsForm;
import io.papermc.hangar.model.internal.api.requests.admin.ChangeRoleForm;
import io.papermc.hangar.model.internal.api.responses.HealthReport;
import io.papermc.hangar.model.internal.logs.HangarLoggedAction;
import io.papermc.hangar.security.annotations.permission.PermissionRequired;
import io.papermc.hangar.security.annotations.ratelimit.RateLimit;
import io.papermc.hangar.security.annotations.unlocked.Unlocked;
import io.papermc.hangar.service.internal.JobService;
import io.papermc.hangar.service.internal.PlatformService;
import io.papermc.hangar.service.internal.admin.HealthService;
import io.papermc.hangar.service.internal.admin.StatService;
import io.papermc.hangar.service.internal.perms.roles.GlobalRoleService;
import io.papermc.hangar.service.internal.projects.ProjectAdminService;
import io.papermc.hangar.service.internal.projects.ProjectFactory;
import io.papermc.hangar.service.internal.projects.ProjectService;
import io.papermc.hangar.service.internal.users.UserService;
import io.papermc.hangar.service.internal.versions.ReviewService;
import io.papermc.hangar.service.internal.versions.VersionService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Unlocked
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
    private final ProjectFactory projectFactory;
    private final ProjectService projectService;
    private final ProjectAdminService projectAdminService;
    private final VersionService versionService;
    private final ReviewService reviewService;
    private final RolesDAO rolesDAO;

    @Autowired
    public AdminController(final PlatformService platformService, final StatService statService, final HealthService healthService, final JobService jobService, final UserService userService, final ObjectMapper mapper, final GlobalRoleService globalRoleService, final ProjectFactory projectFactory, final ProjectService projectService, final ProjectAdminService projectAdminService, final VersionService versionService, final ReviewService reviewService, final RolesDAO rolesDAO) {
        this.platformService = platformService;
        this.statService = statService;
        this.healthService = healthService;
        this.jobService = jobService;
        this.userService = userService;
        this.mapper = mapper;
        this.globalRoleService = globalRoleService;
        this.projectFactory = projectFactory;
        this.projectService = projectService;
        this.projectAdminService = projectAdminService;
        this.versionService = versionService;
        this.reviewService = reviewService;
        this.rolesDAO = rolesDAO;
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping(path = "/platformVersions", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PermissionRequired(NamedPermission.MANUAL_VALUE_CHANGES)
    public void changePlatformVersions(@RequestBody @Valid final ChangePlatformVersionsForm form) {
        this.platformService.updatePlatformVersions(form);
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping(path = "/roles", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PermissionRequired(NamedPermission.MANUAL_VALUE_CHANGES)
    @Transactional
    public void changeRoles(@RequestBody final List<@Valid ChangeRoleForm> roles) {
        for (final ChangeRoleForm role : roles) {
            this.rolesDAO.update(role.roleId(), role.title(), role.color(), role.rank());
        }
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping(path = "/updateHashes")
    @PermissionRequired(NamedPermission.MANUAL_VALUE_CHANGES)
    @RateLimit(overdraft = 1, refillSeconds = RateLimit.MAX_REFILL_DELAY, refillTokens = 1)
    @ResponseBody
    public List<String> updateHashes() {
        return this.versionService.updateFileHashes();
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping(path = "/scanSafeLinks")
    @PermissionRequired(NamedPermission.MANUAL_VALUE_CHANGES)
    @RateLimit(overdraft = 1, refillSeconds = RateLimit.MAX_REFILL_DELAY, refillTokens = 1)
    @ResponseBody
    public void approveVersionsWithSafeLinks() {
        this.reviewService.approveVersionsWithSafeLinks();
    }

    @ResponseBody
    @PermissionRequired(NamedPermission.VIEW_STATS)
    @GetMapping(path = "/stats", produces = MediaType.APPLICATION_JSON_VALUE)
    public ArrayNode getStats(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from, @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        if (from == null) {
            from = LocalDate.now().minusDays(30);
        }
        if (to == null) {
            to = LocalDate.now();
        }
        if (to.isBefore(from)) {
            to = from;
        }
        return this.mapper.valueToTree(this.statService.getStats(from, to));
    }

    @ResponseBody
    @PermissionRequired(NamedPermission.VIEW_HEALTH)
    @GetMapping(path = "/health", produces = MediaType.APPLICATION_JSON_VALUE)
    public HealthReport getHealthReport() {
        if (true) {
            //TODO
            throw new HangarApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Health report is disabled");
        }

        final List<UnhealthyProject> staleProjects = this.healthService.getStaleProjects();
        final List<MissingFileCheck> missingFiles = this.healthService.getVersionsWithMissingFiles();
        final List<JobTable> erroredJobs = this.jobService.getErroredJobs();
        return new HealthReport(staleProjects, missingFiles, erroredJobs);
    }

    @ResponseStatus(HttpStatus.OK)
    @PermissionRequired(NamedPermission.IS_STAFF)
    @PostMapping(value = "/lock-user/{user}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void setUserLock(@PathVariable final UserTable user, @RequestParam final boolean locked, @RequestParam(required = false, defaultValue = "false") final boolean toggleProjectDeletion, @RequestBody @Valid final StringContent comment) {
        if (comment.getContent().length() > 1000) {
            throw new HangarApiException("Comment too long");
        }

        this.userService.setLocked(user, locked, comment.getContent());
        if (!toggleProjectDeletion || !this.getHangarPrincipal().isAllowedGlobal(Permission.DeleteProject)) {
            return;
        }

        for (final ProjectTable projectTable : this.projectService.getProjectTables(user.getUserId())) {
            if (locked) {
                this.projectFactory.softDelete(projectTable, comment.getContent());
            } else {
                this.projectAdminService.changeVisibility(projectTable.getProjectId(), Visibility.PUBLIC, comment.getContent());
            }
        }
    }

    @ResponseStatus(HttpStatus.OK)
    @PermissionRequired(NamedPermission.MANUAL_VALUE_CHANGES)
    @PostMapping(value = "/yeet/{user}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void yeetusDeletus(@PathVariable final UserTable user, @RequestBody @Valid final StringContent comment) {
        if (comment.getContent().length() > 1000) {
            throw new HangarApiException("Comment too long");
        }

        try {
            this.userService.delete(user);
        } catch (final Exception e) {
            e.printStackTrace();
            throw new HangarApiException("Could not delete user", e);
        }
    }

    @ResponseBody
    @GetMapping(value = "/log", produces = MediaType.APPLICATION_JSON_VALUE)
    @PermissionRequired(NamedPermission.REVIEWER)
    @ApplicableFilters({LogActionFilter.class, LogPageFilter.class, LogProjectFilter.class, LogSubjectFilter.class, LogUserFilter.class, LogVersionFilter.class})
    // TODO add sorters
    public PaginatedResult<HangarLoggedAction> getActionLog(@ConfigurePagination(defaultLimit = 50, maxLimit = 100) final @NotNull RequestPagination pagination) {
        return this.actionLogger.getLogs(pagination);
    }

    @ResponseStatus(HttpStatus.OK)
    @PermissionRequired(NamedPermission.EDIT_ALL_USER_SETTINGS)
    @PostMapping("/user/{user}/{role}")
    public void addRole(@PathVariable final UserTable user, @PathVariable final String role) {
        final GlobalRole globalRole = GlobalRole.byApiValue(role);
        if (globalRole.rank() == null) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "Cannot add role with no rank");
        }

        final int rank = this.globalRoleService.getGlobalRoles(this.getHangarUserId()).stream().filter(r -> r.rank() != null).mapToInt(GlobalRole::rank).max().orElse(-1);
        if (rank == -1 || rank != 0 && globalRole.rank() <= rank) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "Cannot add role with higher rank than current highest role");
        }

        this.globalRoleService.addRole(new GlobalRoleTable(user.getUserId(), globalRole));
    }

    @ResponseStatus(HttpStatus.OK)
    @PermissionRequired(NamedPermission.EDIT_ALL_USER_SETTINGS)
    @DeleteMapping("/user/{user}/{role}")
    public void removeRole(@PathVariable final UserTable user, @PathVariable final String role) {
        final GlobalRole globalRole = GlobalRole.byApiValue(role);
        if (globalRole.rank() == null) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "Cannot remove role with no rank");
        }

        final int rank = this.globalRoleService.getGlobalRoles(this.getHangarUserId()).stream().filter(r -> r.rank() != null).mapToInt(GlobalRole::rank).max().orElse(-1);
        if (rank == -1 || rank != 0 && globalRole.rank() <= rank) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "Cannot remove role with higher rank than current highest role");
        }

        this.globalRoleService.deleteRole(new GlobalRoleTable(user.getUserId(), globalRole));
    }
}
