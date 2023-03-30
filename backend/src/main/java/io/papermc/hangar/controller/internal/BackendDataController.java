package io.papermc.hangar.controller.internal;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.config.CacheConfig;
import io.papermc.hangar.config.hangar.HangarConfig;
import io.papermc.hangar.db.customtypes.RoleCategory;
import io.papermc.hangar.db.dao.internal.table.roles.RolesDAO;
import io.papermc.hangar.model.Announcement;
import io.papermc.hangar.model.common.Color;
import io.papermc.hangar.model.common.NamedPermission;
import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.model.common.Prompt;
import io.papermc.hangar.model.common.projects.Category;
import io.papermc.hangar.model.common.projects.FlagReason;
import io.papermc.hangar.model.common.projects.Visibility;
import io.papermc.hangar.model.common.roles.RoleData;
import io.papermc.hangar.model.internal.api.responses.Security;
import io.papermc.hangar.model.internal.api.responses.Validations;
import io.papermc.hangar.model.internal.logs.LogAction;
import io.papermc.hangar.security.annotations.Anyone;
import io.papermc.hangar.security.annotations.ratelimit.RateLimit;
import io.papermc.hangar.service.internal.PlatformService;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.GitProperties;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@Anyone
@RateLimit(path = "backenddata")
@RequestMapping(path = "/api/internal/data", produces = MediaType.APPLICATION_JSON_VALUE)
public class BackendDataController extends HangarComponent {

    private final ObjectMapper objectMapper; // ignores JsonValue annotations
    private final HangarConfig config;
    private final PlatformService platformService;
    private final Optional<GitProperties> gitProperties;
    private final RolesDAO rolesDAO;

    @Autowired
    public BackendDataController(final ObjectMapper mapper, final HangarConfig config, final PlatformService platformService, final Optional<GitProperties> gitProperties, final RolesDAO rolesDAO) {
        this.config = config;
        this.objectMapper = mapper.copy();
        this.platformService = platformService;
        this.gitProperties = gitProperties;
        this.rolesDAO = rolesDAO;
        this.objectMapper.setAnnotationIntrospector(new JacksonAnnotationIntrospector() {
            @Override
            protected <A extends Annotation> A _findAnnotation(final Annotated annotated, final Class<A> annoClass) {
                if (annoClass == JsonValue.class) {
                    return null;
                }
                return super._findAnnotation(annotated, annoClass);
            }
        });
    }

    @GetMapping("/categories")
    @Cacheable(CacheConfig.CATEGORIES)
    public ResponseEntity<ArrayNode> getCategories() {
        return ResponseEntity.ok(this.objectMapper.valueToTree(Category.getValues()));
    }

    @GetMapping("/permissions")
    @Cacheable(CacheConfig.PERMISSIONS)
    public ResponseEntity<ArrayNode> getPermissions() {
        final ArrayNode arrayNode = this.objectMapper.createArrayNode();
        for (final NamedPermission namedPermission : NamedPermission.getValues()) {
            final ObjectNode namedPermissionObject = this.objectMapper.createObjectNode();
            namedPermissionObject.put("value", namedPermission.getValue());
            namedPermissionObject.put("frontendName", namedPermission.getFrontendName());
            namedPermissionObject.put("permission", namedPermission.getPermission().toBinString());
            arrayNode.add(namedPermissionObject);
        }
        return ResponseEntity.ok(arrayNode);
    }

    @GetMapping("/platforms")
    @Cacheable(CacheConfig.PLATFORMS)
    public ResponseEntity<ArrayNode> getPlatforms() {
        final ArrayNode arrayNode = this.objectMapper.createArrayNode();
        for (final Platform platform : Platform.getValues()) {
            final ObjectNode objectNode = this.objectMapper.valueToTree(platform);
            objectNode.set("possibleVersions", this.objectMapper.valueToTree(this.platformService.getDescendingVersionsForPlatform(platform)));
            arrayNode.add(objectNode);
        }
        return ResponseEntity.ok(arrayNode);
    }

    @GetMapping("/channelColors")
    @Cacheable(CacheConfig.CHANNEL_COLORS)
    public ResponseEntity<ArrayNode> getColors() {
        final ArrayNode arrayNode = this.objectMapper.createArrayNode();
        for (final Color color : Color.getNonTransparentValues()) {
            final ObjectNode objectNode = this.objectMapper.createObjectNode()
                .put("name", color.name())
                .put("hex", color.getHex());
            arrayNode.add(objectNode);
        }
        return ResponseEntity.ok(arrayNode);
    }

    @Secured("ROLE_USER")
    @GetMapping("/flagReasons")
    @Cacheable(CacheConfig.FLAG_REASONS)
    public ResponseEntity<ArrayNode> getFlagReasons() {
        final ArrayNode arrayNode = this.objectMapper.createArrayNode();
        for (final FlagReason flagReason : FlagReason.getValues()) {
            final ObjectNode objectNode = this.objectMapper.createObjectNode()
                .put("type", flagReason.name())
                .put("title", flagReason.getTitle());
            arrayNode.add(objectNode);
        }
        return ResponseEntity.ok(arrayNode);
    }

    @GetMapping("/sponsor")
    @Cacheable(CacheConfig.SPONSOR)
    @ResponseBody
    public HangarConfig.Sponsor getSponsor() {
        return this.config.getSponsors().get(ThreadLocalRandom.current().nextInt(this.config.getSponsors().size()));
    }

    @GetMapping("/announcements")
    @Cacheable(CacheConfig.ANNOUNCEMENTS)
    @ResponseBody
    public List<Announcement> getAnnouncements() {
        return this.config.getAnnouncements();
    }

    @GetMapping("/projectRoles")
    @Cacheable(CacheConfig.PROJECT_ROLES)
    @ResponseBody
    public List<RoleData> getProjectRoles() {
        return this.rolesDAO.getRoles(RoleCategory.PROJECT);
    }

    @GetMapping("/globalRoles")
    @Cacheable(CacheConfig.GLOBAL_ROLES)
    @ResponseBody
    public List<RoleData> getGlobalRoles() {
        return this.rolesDAO.getRoles(RoleCategory.GLOBAL);
    }

    @GetMapping("/orgRoles")
    @Cacheable(CacheConfig.ORG_ROLES)
    @ResponseBody
    public List<RoleData> getOrganizationRoles() {
        return this.rolesDAO.getRoles(RoleCategory.ORGANIZATION);
    }

    @GetMapping("/licenses")
    @Cacheable(CacheConfig.LICENSES)
    @ResponseBody
    public List<String> getLicenses() {
        return this.config.getLicenses();
    }

    @GetMapping("/visibilities")
    @Cacheable(CacheConfig.VISIBILITIES)
    public ResponseEntity<ArrayNode> getVisibilities() {
        final ArrayNode arrayNode = this.objectMapper.createArrayNode();
        for (final Visibility value : Visibility.getValues()) {
            final ObjectNode objectNode = this.objectMapper.createObjectNode();
            objectNode.put("name", value.getName())
                .put("showModal", value.shouldShowModal())
                .put("canChangeTo", value.canChangeTo())
                .put("cssClass", value.getCssClass())
                .put("title", value.getTitle());
            arrayNode.add(objectNode);
        }
        return ResponseEntity.ok(arrayNode);
    }

    @ResponseBody
    @GetMapping("/prompts")
    @Cacheable(CacheConfig.PROMPTS)
    public Prompt[] getPrompts() {
        return Prompt.getValues();
    }

    @ResponseBody
    @GetMapping("/version-info")
    @Cacheable(CacheConfig.VERSION_INFO)
    public Map<String, String> info() {
        return Map.of(
            "version", this.get("build.version", -1),
            "committer", this.get("commit.user.name", "dummy"),
            "time", this.get("commit.time", -1),
            "commit", this.gitProperties.map(GitProperties::getCommitId).orElse("0"),
            "commitShort", this.gitProperties.map(GitProperties::getShortCommitId).orElse("0"),
            "message", this.get("commit.message.short", "dummy"),
            "tag", this.gitProperties.map(gp -> gp.get("tags")).or(() -> this.gitProperties.map(gp -> gp.get("closest.tag.name"))).orElse("dummy"),
            "behind", this.get("closest.tag.commit.count", 0)
        );
    }

    private String get(final String propName, final Object fallback) {
        return this.gitProperties.map(gp -> gp.get(propName)).orElse(fallback.toString());
    }

    @GetMapping("/validations")
    @Cacheable(CacheConfig.VALIDATIONS)
    @ResponseBody
    public Validations getValidations() {
        return Validations.create(this.config);
    }

    @GetMapping("/loggedActions")
    @Cacheable(CacheConfig.LOGGED_ACTIONS)
    @ResponseBody
    public List<String> getLoggedActions() {
        return LogAction.LOG_REGISTRY.keySet().stream().sorted().toList();
    }

    @GetMapping("/security")
    @ResponseBody
    public Security getSecurity() {
        return new Security(this.config.security.safeDownloadHosts());
    }
}
