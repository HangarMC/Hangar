package io.papermc.hangar.controllerold.api;

import io.papermc.hangar.config.hangar.HangarConfig;
import io.papermc.hangar.controllerold.exceptions.HangarApiException;
import io.papermc.hangar.modelold.ApiAuthInfo;
import io.papermc.hangar.modelold.Category;
import io.papermc.hangar.modelold.Permission;
import io.papermc.hangar.modelold.generated.PaginatedProjectResult;
import io.papermc.hangar.modelold.generated.Pagination;
import io.papermc.hangar.modelold.generated.Project;
import io.papermc.hangar.modelold.generated.ProjectMember;
import io.papermc.hangar.modelold.generated.ProjectSortingStrategy;
import io.papermc.hangar.modelold.generated.ProjectStatsDay;
import io.papermc.hangar.modelold.generated.Tag;
import io.papermc.hangar.service.api.ProjectApiService;
import io.papermc.hangar.util.ApiUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

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
    private final ProjectApiService projectApiService;

    @Autowired
    public ProjectsApiController(HangarConfig hangarConfig, ApiAuthInfo apiAuthInfo, ProjectApiService projectApiService) {
        this.hangarConfig = hangarConfig;
        this.apiAuthInfo = apiAuthInfo;
        this.projectApiService = projectApiService;
    }

    @Override
    @PreAuthorize("@authenticationService.authApiRequest(T(io.papermc.hangar.modelold.Permission).ViewPublicInfo, T(io.papermc.hangar.controllerold.util.ApiScope).forGlobal())")
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

        List<Project> projects = projectApiService.getProjects(
                owner,
                null,
                categories,
                parsedTags,
                q,
                seeHidden,
                requesterId,
                sort,
                orderWithRelevance,
                limit,
                offset
        );

        long count = projectApiService.countProjects(
                owner,
                null,
                categories,
                parsedTags,
                q,
                seeHidden,
                requesterId
        );

        PaginatedProjectResult result = new PaginatedProjectResult();
        result.setPagination(new Pagination().limit(limit).offset(offset).count(count));
        result.setResult(projects);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Override
    @PreAuthorize("@authenticationService.authApiRequest(T(io.papermc.hangar.modelold.Permission).ViewPublicInfo, T(io.papermc.hangar.controllerold.util.ApiScope).forProject(#author, #slug))")
    public ResponseEntity<List<ProjectMember>> showMembers(String author, String slug, Long inLimit, Long inOffset) {
        long limit = ApiUtil.limitOrDefault(inLimit, hangarConfig.getProjects().getInitLoad());
        long offset = ApiUtil.offsetOrZero(inOffset);
        List<ProjectMember> projectMembers = projectApiService.getProjectMembers(author, slug, limit, offset);
        if (projectMembers == null || projectMembers.isEmpty()) { // TODO this will also happen when the offset is too high
            log.error("Couldn't find a project for that author/slug");
            throw new HangarApiException(HttpStatus.NOT_FOUND, "Couldn't find a project for: " + author + "/" + slug);
        }
        return ResponseEntity.ok(projectMembers);
    }


    @Override
    @PreAuthorize("@authenticationService.authApiRequest(T(io.papermc.hangar.modelold.Permission).ViewPublicInfo, T(io.papermc.hangar.controllerold.util.ApiScope).forProject(#author, #slug))")
    public ResponseEntity<Project> showProject(String author, String slug) {
        boolean seeHidden = apiAuthInfo.getGlobalPerms().has(Permission.SeeHidden);
        Project project = projectApiService.getProject(author, slug, seeHidden, apiAuthInfo.getUserId());
        if (project == null) {
            log.error("Couldn't find a project for that author/slug");
            throw new HangarApiException(HttpStatus.NOT_FOUND, "Couldn't find a project for: " + author + "/" + slug);
        }
        return ResponseEntity.ok(project);
    }

    @Override
    @PreAuthorize("@authenticationService.authApiRequest(T(io.papermc.hangar.modelold.Permission).ViewPublicInfo, T(io.papermc.hangar.controllerold.util.ApiScope).forProject(#id))")
    public ResponseEntity<Project> showProject(long id) {
        boolean seeHidden = apiAuthInfo.getGlobalPerms().has(Permission.SeeHidden);
        Project project = projectApiService.getProject(id, seeHidden, apiAuthInfo.getUserId());
        if (project == null) {
            log.error("Couldn't find a project for that id");
            throw new HangarApiException(HttpStatus.NOT_FOUND, "Couldn't find a project for id: " + id);
        }
        return ResponseEntity.ok(project);
    }

    @Override
    @PreAuthorize("@authenticationService.authApiRequest(T(io.papermc.hangar.modelold.Permission).IsProjectMember, T(io.papermc.hangar.controllerold.util.ApiScope).forProject(#author, #slug))")
    public ResponseEntity<Map<String, ProjectStatsDay>> showProjectStats(String author, String slug, @NotNull @Valid String fromDate, @NotNull @Valid String toDate) {
        LocalDate from = ApiUtil.parseDate(fromDate);
        LocalDate to = ApiUtil.parseDate(toDate);
        if (from.isAfter(to)) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "From date is after to date");
        }
        Map<String, ProjectStatsDay> projectStats = projectApiService.getProjectStats(author, slug, from, to);
        if (projectStats == null || projectStats.isEmpty()) {
            log.error("Couldn't find a project for that author/slug");
            throw new HangarApiException(HttpStatus.NOT_FOUND, "Couldn't find a project for: " + author + "/" + slug);
        }
        return ResponseEntity.ok(projectStats);
    }
}
