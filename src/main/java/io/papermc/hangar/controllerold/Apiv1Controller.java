package io.papermc.hangar.controllerold;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.papermc.hangar.db.customtypes.LoggedActionType;
import io.papermc.hangar.db.customtypes.LoggedActionType.ProjectContext;
import io.papermc.hangar.db.modelold.ProjectApiKeysTable;
import io.papermc.hangar.db.modelold.ProjectsTable;
import io.papermc.hangar.security.annotations.UserLock;
import io.papermc.hangar.service.ApiKeyService;
import io.papermc.hangar.service.UserActionLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;
import java.util.function.Supplier;

@Controller
@RequestMapping("/api/old")
public class Apiv1Controller extends HangarController {


    private final ObjectMapper mapper;
    private final ApiKeyService apiKeyService;
    private final UserActionLogService userActionLogService;

    private final HttpServletRequest request;
    private final Supplier<ProjectsTable> projectsTable;

    @Autowired
    public Apiv1Controller(ObjectMapper mapper, ApiKeyService apiKeyService, UserActionLogService userActionLogService, HttpServletRequest request, Supplier<ProjectsTable> projectsTable) {
        this.mapper = mapper;
        this.apiKeyService = apiKeyService;
        this.userActionLogService = userActionLogService;
        this.request = request;
        this.projectsTable = projectsTable;
    }

    @PreAuthorize("@authenticationService.authV1ApiRequest(T(io.papermc.hangar.model.Permission).EditApiKeys, T(io.papermc.hangar.controller.ApiScope).forProject(#author, #slug))")
    @UserLock
    @Secured("ROLE_USER")
    @PostMapping("/v1/projects/{author}/{slug}/keys/new") // USED IN project settings (deployment key)
    public ResponseEntity<ObjectNode> createKey(@PathVariable String author, @PathVariable String slug) {
        ProjectsTable project = projectsTable.get();
        ProjectApiKeysTable projectApiKeysTable = apiKeyService.createProjectApiKey(new ProjectApiKeysTable(
                project.getId(),
                UUID.randomUUID().toString().replace("-", "")
        ));
        userActionLogService.project(request, LoggedActionType.PROJECT_SETTINGS_CHANGED.with(ProjectContext.of(project.getId())), getCurrentUser().getName() + " created a new ApiKey", "");
        ObjectNode apiKeyObj = mapper.createObjectNode();
        apiKeyObj
                .put("id", projectApiKeysTable.getId())
                .put("createdAt", projectApiKeysTable.getCreatedAt().toString())
                .put("projectId", projectApiKeysTable.getProjectId())
                .put("value", projectApiKeysTable.getValue());
        return ResponseEntity.ok(apiKeyObj);
    }

    @PreAuthorize("@authenticationService.authV1ApiRequest(T(io.papermc.hangar.model.Permission).EditApiKeys, T(io.papermc.hangar.controller.ApiScope).forProject(#author, #slug))")
    @UserLock
    @Secured("ROLE_USER")
    @PostMapping("/v1/projects/{author}/{slug}/keys/revoke") // USED in project settings (deployment key)
    @ResponseStatus(HttpStatus.OK)
    public void revokeKey(@PathVariable String author, @PathVariable String slug, @RequestParam long id) {
        ProjectApiKeysTable projectApiKey = apiKeyService.getProjectKey(id);
        ProjectsTable project = projectsTable.get();
        if (projectApiKey == null || project.getId() != projectApiKey.getId()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        apiKeyService.deleteProjectApiKey(projectApiKey);
        userActionLogService.project(request, LoggedActionType.PROJECT_SETTINGS_CHANGED.with(ProjectContext.of(project.getId())), getCurrentUser().getName() + " removed an ApiKey", "");
    }
}
