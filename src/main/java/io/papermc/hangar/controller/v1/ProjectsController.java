package io.papermc.hangar.controller.v1;

import io.papermc.hangar.controller.extras.requestmodels.api.RequestPagination;
import io.papermc.hangar.model.Category;
import io.papermc.hangar.model.api.PaginatedResult;
import io.papermc.hangar.model.api.project.Project;
import io.papermc.hangar.modelold.generated.ProjectSortingStrategy;
import io.papermc.hangar.service.api.ProjectsService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping(path = {"/api", "/api/v1" }, produces = MediaType.APPLICATION_JSON_VALUE, method = { RequestMethod.GET })
public class ProjectsController {

    private final ProjectsService projectsService;

    @Autowired
    public ProjectsController(ProjectsService projectsService) {
        this.projectsService = projectsService;
    }

    @GetMapping("/projects/{author}/{slug}")
    @PreAuthorize("@authenticationService.handleApiRequest(T(io.papermc.hangar.model.Permission).ViewPublicInfo, T(io.papermc.hangar.controller.extras.ApiScope).ofProject(#author, #slug))")
    public ResponseEntity<Project> getProject(@PathVariable String author, @PathVariable String slug) {
        return ResponseEntity.ok(projectsService.getProject(author, slug));
    }

    @GetMapping("/projects")
    @PreAuthorize("@authenticationService.handleApiRequest(T(io.papermc.hangar.model.Permission).ViewPublicInfo, T(io.papermc.hangar.controller.extras.ApiScope).ofGlobal())")
    public ResponseEntity<PaginatedResult<Project>> getProjects(@RequestParam(required = false) String q,
                                                                @RequestParam(required = false) List<Category> categories,
                                                                @RequestParam(required = false) List<String> tags,
                                                                @RequestParam(required = false) String owner,
                                                                @RequestParam(defaultValue = "updated") ProjectSortingStrategy sort,
                                                                @RequestParam(defaultValue = "true") boolean orderWithRelevance,
                                                                @NotNull RequestPagination pagination) {
        return ResponseEntity.ok(projectsService.getProjects(q, categories, tags, owner, sort, orderWithRelevance, pagination));
    }
}
