package io.papermc.hangar.controller.internal;

import io.papermc.hangar.controller.HangarController;
import io.papermc.hangar.model.common.Permission;
import io.papermc.hangar.model.db.projects.ProjectTable;
import io.papermc.hangar.model.internal.api.requests.projects.NewProject;
import io.papermc.hangar.model.internal.api.responses.PossibleProjectOwner;
import io.papermc.hangar.security.annotations.unlocked.Unlocked;
import io.papermc.hangar.service.internal.OrganizationService;
import io.papermc.hangar.service.internal.projects.ProjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@Secured("ROLE_USER")
@RequestMapping(path = "/api/internal/projects", produces = MediaType.APPLICATION_JSON_VALUE)
public class ProjectController extends HangarController {

    private final ProjectFactory projectFactory;
    private final OrganizationService organizationService;

    @Autowired
    public ProjectController(ProjectFactory projectFactory, OrganizationService organizationService) {
        this.projectFactory = projectFactory;
        this.organizationService = organizationService;
    }

    @GetMapping("/validateName")
    @ResponseStatus(HttpStatus.OK)
    public void validateProjectName(@RequestParam long userId, @RequestParam String projectName) {
        projectFactory.checkProjectAvailability(userId, projectName);
    }

    @GetMapping("/possibleOwners")
    public ResponseEntity<List<PossibleProjectOwner>> possibleProjectCreators() {
        List<PossibleProjectOwner> possibleProjectOwners = organizationService.getOrganizationTablesWithPermission(getHangarPrincipal().getId(), Permission.CreateProject).stream().map(PossibleProjectOwner::new).collect(Collectors.toList());
        possibleProjectOwners.add(new PossibleProjectOwner(getHangarPrincipal()));
        return ResponseEntity.ok(possibleProjectOwners);
    }

    @Unlocked
    @PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> createProject(@RequestBody @Valid NewProject newProject) {
        System.out.println(newProject);
        ProjectTable projectTable = projectFactory.createProject(newProject);
        return ResponseEntity.ok(projectTable.getUrl());
    }
}
