package io.papermc.hangar.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.papermc.hangar.config.hangar.HangarConfig;
import io.papermc.hangar.db.customtypes.LoggedActionType;
import io.papermc.hangar.db.customtypes.LoggedActionType.ProjectContext;
import io.papermc.hangar.db.model.ProjectApiKeysTable;
import io.papermc.hangar.db.model.ProjectChannelsTable;
import io.papermc.hangar.db.model.ProjectVersionTagsTable;
import io.papermc.hangar.db.model.ProjectVersionsTable;
import io.papermc.hangar.db.model.ProjectsTable;
import io.papermc.hangar.db.model.UserProjectRolesTable;
import io.papermc.hangar.db.model.UsersTable;
import io.papermc.hangar.model.Category;
import io.papermc.hangar.model.Platform;
import io.papermc.hangar.model.Role;
import io.papermc.hangar.model.SsoSyncData;
import io.papermc.hangar.model.TagColor;
import io.papermc.hangar.model.Visibility;
import io.papermc.hangar.model.generated.ProjectSortingStrategy;
import io.papermc.hangar.model.viewhelpers.ProjectPage;
import io.papermc.hangar.model.viewhelpers.UserData;
import io.papermc.hangar.security.annotations.UserLock;
import io.papermc.hangar.service.ApiKeyService;
import io.papermc.hangar.service.SsoService;
import io.papermc.hangar.service.SsoService.SignatureException;
import io.papermc.hangar.service.UserActionLogService;
import io.papermc.hangar.service.UserService;
import io.papermc.hangar.service.api.V1ApiService;
import io.papermc.hangar.service.project.PagesSerivce;
import io.papermc.hangar.service.project.ProjectService;
import io.papermc.hangar.util.ApiUtil;
import io.papermc.hangar.util.TemplateHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/api")
public class Apiv1Controller extends HangarController {

    private static final Logger log = LoggerFactory.getLogger(Apiv1Controller.class);

    private final HangarConfig hangarConfig;
    private final ObjectMapper mapper;
    private final TemplateHelper templateHelper;
    private final PagesSerivce pagesSerivce;
    private final ProjectService projectService;
    private final UserService userService;
    private final SsoService ssoService;
    private final V1ApiService v1ApiService;
    private final ApiKeyService apiKeyService;
    private final UserActionLogService userActionLogService;

    private final HttpServletRequest request;
    private final Supplier<ProjectsTable> projectsTable;

    @Autowired
    public Apiv1Controller(HangarConfig hangarConfig, ObjectMapper mapper, TemplateHelper templateHelper, PagesSerivce pagesSerivce, ProjectService projectService, UserService userService, SsoService ssoService, V1ApiService v1ApiService, ApiKeyService apiKeyService, UserActionLogService userActionLogService, HttpServletRequest request, Supplier<ProjectsTable> projectsTable) {
        this.hangarConfig = hangarConfig;
        this.mapper = mapper;
        this.templateHelper = templateHelper;
        this.pagesSerivce = pagesSerivce;
        this.projectService = projectService;
        this.userService = userService;
        this.ssoService = ssoService;
        this.v1ApiService = v1ApiService;
        this.apiKeyService = apiKeyService;
        this.userActionLogService = userActionLogService;
        this.request = request;
        this.projectsTable = projectsTable;
    }

    @GetMapping("/v1/projects")
    public ResponseEntity<ArrayNode> listProjects(@RequestParam(defaultValue = "") List<Category> categories, @RequestParam(defaultValue = "4") int sort, @RequestParam(required = false) String q, @RequestParam(required = false) Long limit, @RequestParam(required = false) Long offset) {
        int maxLoad = hangarConfig.projects.getInitLoad();
        long realLimit = ApiUtil.limitOrDefault(limit, maxLoad);
        long realOffset = ApiUtil.offsetOrZero(offset);
        ProjectSortingStrategy strategy;
        try {
            strategy = ProjectSortingStrategy.VALUES[sort];
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        List<ProjectsTable> sortedProjects = v1ApiService.getProjects(q, categories.stream().map(Category::getValue).collect(Collectors.toList()), strategy, realLimit, realOffset);
        return ResponseEntity.ok(writeProjects(sortedProjects));
    }

    @GetMapping("/v1/projects/{author}/{slug}")
    public ResponseEntity<ObjectNode> showProject(@PathVariable String author, @PathVariable String slug) {
        ProjectsTable project = projectsTable.get();
        return ResponseEntity.ok((ObjectNode) writeProjects(List.of(project)).get(0));
    }

    @GetMapping("v1/projects/{id}")
    public ResponseEntity<ObjectNode> showProject(@PathVariable long id) {
        ProjectsTable project = projectService.getProjectsTable(id);
        return ResponseEntity.ok((ObjectNode) writeProjects(List.of(project)).get(0));
    }

    @PreAuthorize("@authenticationService.authV1ApiRequest(T(io.papermc.hangar.model.Permission).EditApiKeys, T(io.papermc.hangar.controller.util.ApiScope).forProject(#author, #slug))")
    @UserLock
    @Secured("ROLE_USER")
    @PostMapping("/v1/projects/{author}/{slug}/keys/new")
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

    @PreAuthorize("@authenticationService.authV1ApiRequest(T(io.papermc.hangar.model.Permission).EditApiKeys, T(io.papermc.hangar.controller.util.ApiScope).forProject(#author, #slug))")
    @UserLock
    @Secured("ROLE_USER")
    @PostMapping("/v1/projects/{author}/{slug}/keys/revoke")
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

    @GetMapping(value = "/v1/projects/{author}/{slug}/pages", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ArrayNode> listPages(@PathVariable String author, @PathVariable String slug, @RequestParam(required = false) Long parentId) {
        List<ProjectPage> pages = pagesSerivce.getPages(author, slug);
        ArrayNode pagesArray = mapper.createArrayNode();
        pages.stream().filter(p -> {
            if (parentId != null) {
                return parentId.equals(p.getParentId());
            }
            return true;
        }).forEach(page -> {
            ObjectNode pageObj = mapper.createObjectNode();
            String[] pageSlug = page.getSlug().split("/");
            pageObj
                    .put("createdAt", page.getCreatedAt().toString())
                    .put("id", page.getId())
                    .put("name", page.getName())
                    .put("parentId", page.getParentId())
                    .put("slug", pageSlug[pageSlug.length - 1])
                    .put("fullSlug", page.getSlug());
            pagesArray.add(pageObj);
        });
        if (pagesArray.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return ResponseEntity.ok(pagesArray);
    }

    @GetMapping("/v1/projects/{author}/{slug}/versions")
    public ResponseEntity<ArrayNode> listVersions(@PathVariable String author, @PathVariable String slug, @RequestParam(required = false) String channels, @RequestParam(required = false) Long limit, @RequestParam(required = false) Long offset) {
        long realLimit = ApiUtil.limitOrDefault(limit, hangarConfig.projects.getInitLoad());
        long realOffset = ApiUtil.offsetOrZero(offset);

        return ResponseEntity.ok(v1ApiService.getVersionList(channels, realLimit, realOffset, true));
    }

    @GetMapping("/v1/projects/{author}/{slug}/versions/{name:.*}")
    public ResponseEntity<ObjectNode> showVersion(@PathVariable String author, @PathVariable String slug, @PathVariable String name) {
        return ResponseEntity.ok(v1ApiService.getVersion());
    }

    @Secured("ROLE_USER")
    @PostMapping("/v1/projects/{author}/{slug}/versions/{name:.*}")
    public Object deployVersion(@PathVariable String author, @PathVariable String slug, @PathVariable String name) {
        return null; // TODO implement deployVersion request controller.
    }

    @GetMapping("/v1/projects/{author}/{slug}/tags/{name:.*}")
    public ResponseEntity<ObjectNode> listTags(@PathVariable String author, @PathVariable String slug, @PathVariable String name) {
        return ResponseEntity.ok(v1ApiService.getVersionTags(author, slug));
    }

    @GetMapping("/v1/tags/{tagId}")
    public ResponseEntity<ObjectNode> tagColor(@PathVariable("tagId") TagColor tag) {
        return ResponseEntity.of(Optional.of(writeTagColor(tag)));
    }

    @GetMapping("/v1/platforms")
    public ResponseEntity<ArrayNode> platformList() {
        ArrayNode platforms = mapper.createArrayNode();
        for (Platform pl : Platform.getValues()) {
            ObjectNode platformObj = mapper.createObjectNode()
                    .put("id", pl.ordinal())
                    .put("name", pl.getName())
                    .put("category", pl.getPlatformCategory().getName());
            platformObj.set("possibleVersions", mapper.valueToTree(pl.getPossibleVersions()));
            platformObj.set("tag", writeTagColor(pl.getTagColor()));
            platforms.add(platformObj);
        }
        return ResponseEntity.ok(platforms);
    }

    @GetMapping("/v1/users")
    public ResponseEntity<ArrayNode> listUsers(@RequestParam(defaultValue = "0") int offset, @RequestParam(required = false) Integer limit) {
        return ResponseEntity.of(Optional.of(writeUsers(v1ApiService.getUsers(offset, limit))));
    }

    @GetMapping("/v1/users/{user}")
    @ResponseBody
    public ResponseEntity<ObjectNode> showUser(@PathVariable String user) {
        UserData userData = userService.getUserData(user);
        JsonNode userObj = writeUsers(List.of(userData.getUser())).get(0);
        if (userObj == null || !userObj.isObject()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok((ObjectNode) userObj);
    }

    private ObjectNode writeTagColor(TagColor tagColor) {
        return mapper.createObjectNode()
                .put("id", tagColor.ordinal())
                .put("background", tagColor.getBackground())
                .put("foreground", tagColor.getForeground());
    }

    private ArrayNode writeUsers(List<UsersTable> usersTables) {
        ArrayNode usersArray = mapper.createArrayNode();
        List<Long> userIds = usersTables.stream().map(UsersTable::getId).collect(Collectors.toList());
        Map<Long, List<ProjectsTable>> usersProjects = v1ApiService.getUsersProjects(userIds);
        Map<Long, List<String>> projectStars = v1ApiService.getStarredPlugins(userIds);
        Map<Long, List<Role>> globalRoles = v1ApiService.getUsersGlobalRoles(userIds);
        usersTables.forEach(user -> {
            ObjectNode userObj = mapper.createObjectNode()
                    .put("id", user.getId())
                    .put("createdAt", user.getCreatedAt().toString())
                    .put("username", user.getName())
                    .put("avatarUrl", templateHelper.avatarUrl(user.getName()));
            userObj.set("roles", mapper.valueToTree(globalRoles.getOrDefault(user.getId(), new ArrayList<>()).stream().map(Role::getTitle)));
            userObj.set("starred", mapper.valueToTree(projectStars.getOrDefault(user.getId(), new ArrayList<>())));
            userObj.set("projects", writeProjects(usersProjects.getOrDefault(user.getId(), new ArrayList<>())));
            usersArray.add(userObj);
        });
        return usersArray;
    }

    private ArrayNode writeProjects(List<ProjectsTable> projectsTables) {
        ArrayNode projectsArray = mapper.createArrayNode();
        List<Long> projectIds = projectsTables.stream().map(ProjectsTable::getId).collect(Collectors.toList());
        Map<Long, List<ProjectChannelsTable>> projectChannels = v1ApiService.getProjectsChannels(projectIds);

        Map<Long, List<Entry<UserProjectRolesTable, UsersTable>>> members = v1ApiService.getProjectsMembers(projectIds);
        Map<Long, ProjectVersionsTable> recommendedVersions = v1ApiService.getProjectsRecommendedVersion(projectIds);
        Map<Long, ProjectChannelsTable> recommendedVersionChannels = v1ApiService.getProjectsRecommendedVersionChannel(projectIds);

        Map<Long, List<ProjectVersionTagsTable>> vTags = v1ApiService.getVersionsTags(recommendedVersions.values().stream().map(ProjectVersionsTable::getId).collect(Collectors.toList()));

        projectsTables.forEach(project -> {
            ObjectNode projectObj = mapper.createObjectNode();
            projectObj.put("id", project.getId())
                    .put("author", project.getOwnerName())
                    .put("slug", project.getSlug())
                    .put("createdAt", project.getCreatedAt().toString())
                    .put("name", project.getName())
                    .put("description", project.getDescription())
                    .put("href", project.getOwnerName() + "/" + project.getSlug())
                    .put("views", 0)
                    .put("downloads", 0)
                    .put("stars", 0);
            projectObj.set("members", writeMembers(members.getOrDefault(project.getId(), new ArrayList<>())));
            projectObj.set("channels", mapper.valueToTree( projectChannels.getOrDefault(project.getId(), new ArrayList<>())));
            projectObj.set("recommended", writeVersion(project, recommendedVersions, recommendedVersionChannels, vTags));
            ObjectNode projectCategoryObj = mapper.createObjectNode()
                    .put("title", project.getCategory().getTitle())
                    .put("icon", project.getCategory().getIcon());
            projectObj.set("category", projectCategoryObj);

            projectsArray.add(projectObj);
        });
        return projectsArray;
    }

    private ObjectNode writeVersion(ProjectsTable project, Map<Long, ProjectVersionsTable> recommendedVersions, Map<Long, ProjectChannelsTable> recommendedVersionChannels,  Map<Long, List<ProjectVersionTagsTable>> vTags) {
        ProjectVersionsTable version = recommendedVersions.get(project.getId());
        ObjectNode objectNode = mapper.createObjectNode();
        if (version == null) return objectNode;
        ProjectChannelsTable channel = recommendedVersionChannels.get(project.getId());
        List<ProjectVersionTagsTable> tags = vTags.getOrDefault(version.getId(), new ArrayList<>());

        objectNode.put("id", version.getId())
                .put("author", project.getOwnerName())
                .put("slug", project.getSlug())
                .put("createdAt", version.getCreatedAt().toString())
                .put("name", version.getVersionString())
                .put("fileSize", version.getFileSize())
                .put("md5", version.getHash())
                .put("staffApproved", version.getReviewState().isChecked())
                .put("reviewState", version.getReviewState().toString())
                .put("href", "/" + project.getOwnerName() + "/" + project.getSlug() + "/versions/" + version.getVersionString())
                .put("downloads", 0)
                .put("description", version.getDescription());
        objectNode.set("channel", mapper.valueToTree(channel));
        objectNode.set("dependencies", mapper.valueToTree(version.getDependencies()));
        objectNode.set("tags", tags.stream().collect(Collector.of(mapper::createArrayNode, (array, tag) -> array.add(mapper.valueToTree(tag)), (t1, t2) -> {
            throw new UnsupportedOperationException();
        })));

        if (version.getVisibility() != Visibility.PUBLIC) {
            ObjectNode visObj = mapper.createObjectNode()
                    .put("type", version.getVisibility().getName())
                    .put("css", version.getVisibility().getCssClass());
            objectNode.set("visibility", visObj);
        }

        return objectNode;
    }

    private ArrayNode writeMembers(List<Entry<UserProjectRolesTable, UsersTable>> members) {
        ArrayNode membersArray = mapper.createArrayNode();

        Map<Long, List<Role>> allRoles = new HashMap<>();
        members.forEach(entry -> {
            if (allRoles.containsKey(entry.getValue().getId())) {
                allRoles.get(entry.getValue().getId()).add(Role.fromId(entry.getKey().getId()));
            } else {
                allRoles.put(entry.getValue().getId(), new ArrayList<>(Collections.singletonList(Role.fromId(entry.getKey().getId()))));
            }
        });

        members.forEach(member -> {
            List<Role> roles = allRoles.getOrDefault(member.getValue().getId(), new ArrayList<>());

            ObjectNode memberObj = mapper.createObjectNode()
                    .put("userId", member.getValue().getId())
                    .put("name", member.getValue().getName());
            memberObj.set("roles", mapper.valueToTree(roles.stream().map(Role::getTitle).collect(Collectors.toList())));
            if (roles.isEmpty()) {
                memberObj.set("headRole", mapper.valueToTree(null));
            } else {
                memberObj.set("headRole",  mapper.valueToTree(roles.stream().max(Comparator.comparingLong(role -> role.getPermissions().getValue())).get().getTitle()));
            }
            membersArray.add(memberObj);
        });
        return membersArray;
    }

    @PostMapping("/sync_sso")
    public ResponseEntity<MultiValueMap<String, String>> syncSso(@RequestParam String sso, @RequestParam String sig, @RequestParam("api_key") String apiKey) {
        if (!apiKey.equals(hangarConfig.sso.getApiKey())) {
            log.warn("SSO sync failed: bad API key ({} provided, {} expected)", apiKey, hangarConfig.sso.getApiKey());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        try {
            Map<String, String> map = ssoService.decode(sso, sig);
            SsoSyncData data = SsoSyncData.fromSignedPayload(map);
            userService.ssoSyncUser(data);
            log.debug("SSO sync successful: {}", map.toString());
            MultiValueMap<String, String> ssoResponse = new LinkedMultiValueMap<>();
            ssoResponse.set("status", "success");
            return new ResponseEntity<>(ssoResponse, HttpStatus.OK);
        } catch (SignatureException e) {
            log.warn("SSO sync failed: invalid signature ({} for data {})", sig, sso);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
