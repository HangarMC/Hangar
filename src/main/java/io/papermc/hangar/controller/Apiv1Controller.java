package io.papermc.hangar.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.papermc.hangar.config.hangar.HangarConfig;
import io.papermc.hangar.db.model.ProjectChannelsTable;
import io.papermc.hangar.db.model.ProjectVersionTagsTable;
import io.papermc.hangar.db.model.ProjectVersionsTable;
import io.papermc.hangar.db.model.ProjectsTable;
import io.papermc.hangar.db.model.UserProjectRolesTable;
import io.papermc.hangar.db.model.UsersTable;
import io.papermc.hangar.model.TagColor;
import io.papermc.hangar.model.SsoSyncData;
import io.papermc.hangar.model.Visibility;
import io.papermc.hangar.service.SsoService;
import io.papermc.hangar.service.SsoService.SignatureException;
import io.papermc.hangar.service.UserService;
import io.papermc.hangar.service.api.V1ApiService;
import io.papermc.hangar.util.TemplateHelper;
import io.papermc.hangar.model.Role;
import io.papermc.hangar.model.generated.Dependency;
import io.papermc.hangar.model.viewhelpers.ProjectPage;
import io.papermc.hangar.model.viewhelpers.UserData;
import io.papermc.hangar.service.project.PagesSerivce;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
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
    private final UserService userService;
    private final SsoService ssoService;

    private final V1ApiService v1ApiService;

    @Autowired
    public Apiv1Controller(HangarConfig hangarConfig, ObjectMapper mapper, TemplateHelper templateHelper, PagesSerivce pagesSerivce, UserService userService, SsoService ssoService, V1ApiService v1ApiService) {
        this.hangarConfig = hangarConfig;
        this.mapper = mapper;
        this.templateHelper = templateHelper;
        this.pagesSerivce = pagesSerivce;
        this.userService = userService;
        this.ssoService = ssoService;
        this.v1ApiService = v1ApiService;
    }

    @RequestMapping("/v1/projects")
    public Object listProjects(@RequestParam Object categories, @RequestParam Object sort, @RequestParam Object q, @RequestParam Object limit, @RequestParam Object offset) {
        return null; // TODO implement listProjects request controller
    }

    @RequestMapping("/v1/projects/{pluginId}")
    public Object showProject(@PathVariable Object pluginId) {
        return null; // TODO implement showProject request controller
    }

    @Secured("ROLE_USER")
    @RequestMapping("/v1/projects/{pluginId}/keys/new")
    public Object createKey(@PathVariable Object pluginId) {
        return null; // TODO implement createKey request controller
    }

    @Secured("ROLE_USER")
    @RequestMapping("/v1/projects/{pluginId}/keys/revoke")
    public Object revokeKey(@PathVariable Object pluginId) {
        return null; // TODO implement revokeKey request controller
    }

    @GetMapping(value = "/v1/projects/{pluginId}/pages", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ArrayNode> listPages(@PathVariable String pluginId, @RequestParam(required = false) Long parentId) {
        List<ProjectPage> pages = pagesSerivce.getPages(pluginId);
        ArrayNode pagesArray = mapper.createArrayNode();
        pages.stream().filter(p -> {
            if (parentId != null) {
                return parentId.equals(p.getParentId());
            } else return true;
        }).forEach(page -> {
            ObjectNode pageObj = mapper.createObjectNode();
            pageObj.set("createdAt", mapper.valueToTree(page.getCreatedAt()));
            pageObj.set("id", mapper.valueToTree(page.getId()));
            pageObj.set("name", mapper.valueToTree(page.getName()));
            pageObj.set("parentId", mapper.valueToTree(page.getParentId()));
            String[] slug = page.getSlug().split("/");
            pageObj.set("slug", mapper.valueToTree(slug[slug.length - 1]));
            pageObj.set("fullSlug", mapper.valueToTree(page.getSlug()));
            pagesArray.add(pageObj);
        });
        if (pagesArray.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return ResponseEntity.ok(pagesArray);
    }

    @RequestMapping("/v1/projects/{pluginId}/versions")
    public Object listVersions(@PathVariable Object pluginId, @RequestParam Object channels, @RequestParam Object limit, @RequestParam Object offset) {
        return null; // TODO implement listVersions request controller
    }

    @RequestMapping("/v1/projects/{pluginId}/versions/{name}")
    public Object showVersion(@PathVariable Object pluginId, @PathVariable Object name) {
        return null; // TODO implement showVersion request controller
    }

    @Secured("ROLE_USER")
    @PostMapping("/v1/projects/{pluginId}/versions/{name}")
    public Object deployVersion(@PathVariable Object pluginId, @PathVariable Object name) {
        return null; // TODO implement deployVersion request controller
    }

    @RequestMapping("/v1/projects/{plugin}/tags/{versionName}")
    public Object listTags(@PathVariable Object plugin, @PathVariable Object versionName) {
        return null; // TODO implement listTags request controller
    }

    @RequestMapping("/v1/tags/{tagId}")
    public ResponseEntity<ObjectNode> tagColor(@PathVariable("tagId") TagColor tag) {
        ObjectNode tagColor = mapper.createObjectNode();
        tagColor.set("id", mapper.valueToTree(tag.ordinal()));
        tagColor.set("backgroundColor", mapper.valueToTree(tag.getBackground()));
        tagColor.set("foregroundColor", mapper.valueToTree(tag.getForeground()));
        return ResponseEntity.of(Optional.of(tagColor));
    }

    @RequestMapping("/v1/users")
    public ResponseEntity<ArrayNode> listUsers(@RequestParam(defaultValue = "0") int offset, @RequestParam(required = false) Integer limit) {
        return ResponseEntity.of(Optional.of(writeUsers(v1ApiService.getUsers(offset, limit))));
    }

    @RequestMapping("/v1/users/{user}")
    @ResponseBody
    public ResponseEntity<ObjectNode> showUser(@PathVariable String user) {
        UserData userData = userService.getUserData(user);
        JsonNode userObj = writeUsers(List.of(userData.getUser())).get(0);
        if (userObj == null || !userObj.isObject()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok((ObjectNode) userObj);
    }

    @RequestMapping("/statusz")
    public Object showStatusZ() {
        return null; // TODO implement showStatusZ request controller
    }

    private ArrayNode writeUsers(List<UsersTable> usersTables) {
        ArrayNode usersArray = mapper.createArrayNode();
        List<Long> userIds = usersTables.stream().map(UsersTable::getId).collect(Collectors.toList());
        Map<Long, List<ProjectsTable>> usersProjects = v1ApiService.getUsersProjects(userIds);
        Map<Long, List<String>> projectStars = v1ApiService.getStarredPlugins(userIds);
        Map<Long, List<Role>> globalRoles = v1ApiService.getUsersGlobalRoles(userIds);
        usersTables.forEach(user -> {
            ObjectNode userObj = mapper.createObjectNode();
            userObj.set("id", mapper.valueToTree(user.getId()));
            userObj.set("createdAt", mapper.valueToTree(user.getCreatedAt()));
            userObj.set("username", mapper.valueToTree(user.getName()));
            userObj.set("roles", mapper.valueToTree(globalRoles.getOrDefault(user.getId(), new ArrayList<>()).stream().map(Role::getTitle)));
            userObj.set("starred", mapper.valueToTree(projectStars.getOrDefault(user.getId(), new ArrayList<>())));
            userObj.set("avatarUrl", mapper.valueToTree(templateHelper.avatarUrl(user.getName())));
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

        Map<Long, List<ProjectVersionTagsTable>> vTags = v1ApiService.getVersionsTags(recommendedVersions.entrySet().stream().map(entry -> entry.getValue().getId()).collect(Collectors.toList()));

        projectsTables.forEach(project -> {
            ObjectNode projectObj = mapper.createObjectNode();
            projectObj.set("pluginId", mapper.valueToTree(project.getPluginId()));
            projectObj.set("createdAt", mapper.valueToTree(project.getCreatedAt().toString()));
            projectObj.set("name", mapper.valueToTree(project.getName()));
            projectObj.set("owner", mapper.valueToTree(project.getOwnerName()));
            projectObj.set("description", mapper.valueToTree(project.getDescription()));
            projectObj.set("href", mapper.valueToTree(project.getOwnerName() + "/" + project.getSlug()));
            projectObj.set("members", writeMembers(members.getOrDefault(project.getId(), new ArrayList<>())));
            projectObj.set("channels", mapper.valueToTree( projectChannels.getOrDefault(project.getId(), new ArrayList<>())));
            projectObj.set("recommended", writeVersion(project, recommendedVersions, recommendedVersionChannels, vTags));
            ObjectNode projectCategoryObj = mapper.createObjectNode();
            projectCategoryObj.set("title", mapper.valueToTree(project.getCategory().getTitle()));
            projectCategoryObj.set("icon", mapper.valueToTree(project.getCategory().getIcon()));
            projectObj.set("category", projectCategoryObj);
            projectObj.set("views", mapper.valueToTree(0));
            projectObj.set("downloads", mapper.valueToTree(0));
            projectObj.set("stars", mapper.valueToTree(0));
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

        objectNode.set("id", mapper.valueToTree(version.getId()));
        objectNode.set("createdAt", mapper.valueToTree(version.getCreatedAt().toString()));
        objectNode.set("name", mapper.valueToTree(version.getVersionString()));
        objectNode.set("dependencies", Dependency.from(version.getDependencies()).stream().collect(Collector.of(mapper::createArrayNode, (array, dep) -> {
            ObjectNode depObj = mapper.createObjectNode();
            depObj.set("pluginId", mapper.valueToTree(dep.getPluginId()));
            depObj.set("version", mapper.valueToTree(dep.getVersion()));
            array.add(depObj);
        }, (ignored1, ignored2) -> {
            throw new UnsupportedOperationException();
        })));
        objectNode.set("pluginId", mapper.valueToTree(project.getPluginId()));
        objectNode.set("channel", mapper.valueToTree(channel));
        objectNode.set("fileSize", mapper.valueToTree(version.getFileSize()));
        objectNode.set("md5", mapper.valueToTree(version.getHash()));
        objectNode.set("staffApproved", mapper.valueToTree(version.getReviewState().isChecked()));
        objectNode.set("reviewState", mapper.valueToTree(version.getReviewState().toString()));
        objectNode.set("href", mapper.valueToTree("/" + project.getOwnerName() + "/" + project.getSlug() + "/versions/" + version.getVersionString()));
        objectNode.set("tags", tags.stream().collect(Collector.of(mapper::createArrayNode, (array, tag) -> array.add(mapper.valueToTree(tag)), (t1, t2) -> {
            throw new UnsupportedOperationException();
        })));
        objectNode.set("downloads", mapper.valueToTree(0));
        objectNode.set("description", mapper.valueToTree(version.getDescription()));

        if (version.getVisibility() != Visibility.PUBLIC) {
            ObjectNode visObj = mapper.createObjectNode();
            visObj.set("type", mapper.valueToTree(version.getVisibility().getName()));
            visObj.set("css", mapper.valueToTree(version.getVisibility().getCssClass()));
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

            ObjectNode memberObj = mapper.createObjectNode();
            memberObj.set("userId", mapper.valueToTree(member.getValue().getId()));
            memberObj.set("name", mapper.valueToTree(member.getValue().getName()));
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

    @PostMapping(value = "/sync_sso")
    public ResponseEntity<ObjectNode> syncSso(@RequestParam String sso, @RequestParam String sig, @RequestParam String apiKey) {
        if (!apiKey.equals(hangarConfig.sso.getApiKey())) {
            log.warn("SSO sync failed: bad API key (" + apiKey + " provided, " + hangarConfig.sso.getApiKey() + " expected)");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        try {
            Map<String, String> map = ssoService.decode(sso, sig);
            SsoSyncData data = SsoSyncData.fromSignedPayload(map);
            userService.ssoSyncUser(data);
            log.debug("SSO sync successful: " + map.toString());
            return new ResponseEntity<>(mapper.createObjectNode().set("status", mapper.valueToTree("success")), HttpStatus.OK);
        } catch (SignatureException e) {
            log.warn("SSO sync failed: invalid signature (" + sig + " for data " + sso + ")");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
