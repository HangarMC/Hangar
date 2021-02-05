package io.papermc.hangar.controllerold.api;

import io.papermc.hangar.config.hangar.HangarConfig;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.Permission;
import io.papermc.hangar.modelold.ApiAuthInfo;
import io.papermc.hangar.modelold.generated.Project;
import io.papermc.hangar.modelold.generated.ProjectStatsDay;
import io.papermc.hangar.serviceold.apiold.ProjectApiService;
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

//    @Override
//    @PreAuthorize("@authenticationService.authApiRequest(T(io.papermc.hangar.model.Permission).ViewPublicInfo, T(io.papermc.hangar.controller.extras.ApiScope).ofProject(#author, #slug))")
//    public ResponseEntity<List<ProjectMember>> showMembers(String author, String slug, Long inLimit, Long inOffset) {
//        long limit = ApiUtil.limitOrDefault(inLimit, hangarConfig.getProjects().getInitLoad());
//        long offset = ApiUtil.offsetOrZero(inOffset);
//        List<ProjectMember> projectMembers = projectApiService.getProjectMembers(author, slug, limit, offset);
//        if (projectMembers == null || projectMembers.isEmpty()) { // TODO this will also happen when the offset is too high
//            log.error("Couldn't find a project for that author/slug");
//            throw new HangarApiException(HttpStatus.NOT_FOUND, "Couldn't find a project for: " + author + "/" + slug);
//        }
//        return ResponseEntity.ok(projectMembers);
//    }

    @Override
    @PreAuthorize("@authenticationService.authApiRequest(T(io.papermc.hangar.model.Permission).ViewPublicInfo, T(io.papermc.hangar.controller.extras.ApiScope).ofProject(#id))")
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
    @PreAuthorize("@authenticationService.authApiRequest(T(io.papermc.hangar.model.Permission).IsProjectMember, T(io.papermc.hangar.controller.extras.ApiScope).ofProject(#author, #slug))")
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
