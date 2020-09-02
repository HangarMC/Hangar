package io.papermc.hangar.controller.api;

import io.papermc.hangar.config.hangar.HangarConfig;
import io.papermc.hangar.model.ApiAuthInfo;
import io.papermc.hangar.model.Category;
import io.papermc.hangar.model.Permission;
import io.papermc.hangar.model.generated.PaginatedProjectResult;
import io.papermc.hangar.model.generated.Pagination;
import io.papermc.hangar.model.generated.Project;
import io.papermc.hangar.model.generated.ProjectMember;
import io.papermc.hangar.model.generated.ProjectSortingStrategy;
import io.papermc.hangar.model.generated.ProjectStatsDay;
import io.papermc.hangar.model.generated.Tag;
import io.papermc.hangar.service.api.ProjectApiService;
import io.papermc.hangar.service.project.ProjectService;
import io.papermc.hangar.util.ApiUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class ProjectsApiController implements ProjectsApi {

    private static final Logger log = LoggerFactory.getLogger(ProjectsApiController.class);

    private final HangarConfig hangarConfig;
    private final ApiAuthInfo apiAuthInfo;
    private final ProjectService projectService;
    private final ProjectApiService projectApiService;

    @Autowired
    public ProjectsApiController(HangarConfig hangarConfig, ApiAuthInfo apiAuthInfo, ProjectService projectService, ProjectApiService projectApiService) {
        this.hangarConfig = hangarConfig;
        this.apiAuthInfo = apiAuthInfo;
        this.projectService = projectService;
        this.projectApiService = projectApiService;
    }

    @Override
    @PreAuthorize("@authenticationService.authApiRequest(T(io.papermc.hangar.model.Permission).ViewPublicInfo, T(io.papermc.hangar.controller.util.ApiScope).forGlobal())")
    public ResponseEntity<PaginatedProjectResult> listProjects(String q, List<Category> categories, List<String> tags, String owner, ProjectSortingStrategy sort, boolean orderWithRelevance, Long inLimit, Long inOffset) {
        // handle input
        long limit = ApiUtil.limitOrDefault(inLimit, hangarConfig.getProjects().getInitLoad());
        long offset = ApiUtil.offsetOrZero(inOffset);

        // parse tags
        List<Tag> parsedTags = new ArrayList<>();
        if (tags == null) {
            tags = new ArrayList<>();
        }
        for (String tag : tags) {
            String[] split = tag.split(":", 2);
            parsedTags.add(new Tag().name(split[0]).data(split.length > 1 ? split[1] : null));
        }

        boolean seeHidden = apiAuthInfo.getGlobalPerms().has(Permission.SeeHidden);
        Long requesterId = apiAuthInfo.getUser() == null ? null : apiAuthInfo.getUser().getId();

        List<Project> projects = projectService.getProjects(
                null,
                categories,
                parsedTags,
                q,
                owner,
                seeHidden,
                requesterId,
                sort,
                orderWithRelevance,
                limit,
                offset
        );

        long count = projectService.countProjects(
                null,
                categories,
                parsedTags,
                q,
                owner,
                seeHidden,
                requesterId
        );

        PaginatedProjectResult result = new PaginatedProjectResult();
        result.setPagination(new Pagination().limit(limit).offset(offset).count(count));
        result.setResult(projects);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Override
    @PreAuthorize("@authenticationService.authApiRequest(T(io.papermc.hangar.model.Permission).ViewPublicInfo, T(io.papermc.hangar.controller.util.ApiScope).forProject(#pluginId))")
    public ResponseEntity<List<ProjectMember>> showMembers(String pluginId, Long inLimit, Long inOffset) {
        long limit = ApiUtil.limitOrDefault(inLimit, hangarConfig.getProjects().getInitLoad());
        long offset = ApiUtil.offsetOrZero(inOffset);
        List<ProjectMember> projectMembers = projectApiService.getProjectMembers(pluginId, limit, offset);
        if (projectMembers == null || projectMembers.isEmpty()) { // TODO this will also happen when the offset is too high
            log.error("Couldn't find a project for that pluginId");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(projectMembers);
    }


    @Override
    @PreAuthorize("@authenticationService.authApiRequest(T(io.papermc.hangar.model.Permission).ViewPublicInfo, T(io.papermc.hangar.controller.util.ApiScope).forProject(#pluginId))")
    public ResponseEntity<Project> showProject(String pluginId) {
        Project project = projectService.getProjectApi(pluginId);
        if (project == null) {
            log.error("Couldn't find a project for that pluginId");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(project);
    }


    @Override
    @PreAuthorize("@authenticationService.authApiRequest(T(io.papermc.hangar.model.Permission).IsProjectMember, T(io.papermc.hangar.controller.util.ApiScope).forProject(#pluginId))")
    public ResponseEntity<Map<String, ProjectStatsDay>> showProjectStats(String pluginId, @NotNull @Valid String fromDate, @NotNull @Valid String toDate) {
        LocalDate from = ApiUtil.parseDate(fromDate);
        LocalDate to = ApiUtil.parseDate(toDate);
        if (from.isAfter(to)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "From date is after to date");
        }
        Map<String, ProjectStatsDay> projectStats = projectApiService.getProjectStats(pluginId, from, to);
        if (projectStats == null || projectStats.size() == 0) {
            log.error("Couldn't find a project for that pluginId");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(projectStats);
    }
}
