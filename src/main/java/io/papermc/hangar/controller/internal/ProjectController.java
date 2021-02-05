package io.papermc.hangar.controller.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.papermc.hangar.controller.HangarController;
import io.papermc.hangar.model.Permission;
import io.papermc.hangar.model.internal.api.responses.PossibleProjectOwner;
import io.papermc.hangar.service.internal.OrganizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@Secured("ROLE_USER")
@RequestMapping(path = "/api/internal", produces = MediaType.APPLICATION_JSON_VALUE)
public class ProjectController extends HangarController {

    private final OrganizationService organizationService;

    @Autowired
    public ProjectController(OrganizationService organizationService, ObjectMapper mapper) {
        this.organizationService = organizationService;
    }

    @GetMapping("/projects/validateName")
    @ResponseStatus(HttpStatus.OK)
    public void validateProjectName(@RequestParam long userId, @RequestParam String projectName) {

    }

    // TODO better name for this?
    @GetMapping("/projects/possibleOwners")
    public ResponseEntity<List<PossibleProjectOwner>> possibleProjectCreators() {
        List<PossibleProjectOwner> possibleProjectOwners = organizationService.getOrganizationTablesWithPermission(getHangarPrincipal().getId(), Permission.CreateProject).stream().map(PossibleProjectOwner::new).collect(Collectors.toList());
        possibleProjectOwners.add(new PossibleProjectOwner(getHangarPrincipal()));
        return ResponseEntity.ok(possibleProjectOwners);
    }
}
