package io.papermc.hangar.controller.internal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.papermc.hangar.config.hangar.HangarConfig;
import io.papermc.hangar.model.Announcement;
import io.papermc.hangar.model.common.Color;
import io.papermc.hangar.model.common.NamedPermission;
import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.model.common.Prompt;
import io.papermc.hangar.model.common.projects.Category;
import io.papermc.hangar.model.common.projects.FlagReason;
import io.papermc.hangar.model.common.projects.Visibility;
import io.papermc.hangar.model.common.roles.OrganizationRole;
import io.papermc.hangar.model.common.roles.ProjectRole;
import io.papermc.hangar.security.annotations.Anyone;
import io.papermc.hangar.service.internal.PlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Controller
@Anyone
@RequestMapping(path = "/api/internal/data", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
public class BackendDataController {

    private final ObjectMapper noJsonValueMapper;
    private final HangarConfig config;
    private final PlatformService platformService;

    @Autowired
    public BackendDataController(ObjectMapper mapper, HangarConfig config, PlatformService platformService) {
        this.config = config;
        this.noJsonValueMapper = mapper.copy();
        this.platformService = platformService;
        this.noJsonValueMapper.setAnnotationIntrospector(new JacksonAnnotationIntrospector() {
            @Override
            protected <A extends Annotation> A _findAnnotation(Annotated annotated, Class<A> annoClass) {
                if (!annotated.hasAnnotation(JsonValue.class)) {
                    return super._findAnnotation(annotated, annoClass);
                }
                return null;
            }
        });
    }

    @GetMapping("/categories")
    public ResponseEntity<ArrayNode> getCategories() {
        return ResponseEntity.ok(noJsonValueMapper.valueToTree(Category.getValues()));
    }

    @GetMapping("/permissions")
    public ResponseEntity<ArrayNode> getPermissions() {
        ArrayNode arrayNode = noJsonValueMapper.createArrayNode();
        for (NamedPermission namedPermission : NamedPermission.getValues()) {
            ObjectNode namedPermissionObject = noJsonValueMapper.createObjectNode();
            namedPermissionObject.put("value", namedPermission.getValue());
            namedPermissionObject.put("frontendName", namedPermission.getFrontendName());
            namedPermissionObject.put("permission", namedPermission.getPermission().toBinString());
            arrayNode.add(namedPermissionObject);
        }
        return ResponseEntity.ok(arrayNode);
    }

    @GetMapping("/platforms")
    public ResponseEntity<ArrayNode> getPlatforms() {
        ArrayNode arrayNode = noJsonValueMapper.createArrayNode();
        for (Platform platform : Platform.getValues()) {
            ObjectNode objectNode = noJsonValueMapper.valueToTree(platform);
            objectNode.set("tagColor", noJsonValueMapper.valueToTree(platform.getTagColor()));
            objectNode.set("possibleVersions", noJsonValueMapper.valueToTree(platformService.getVersionsForPlatform(platform)));
            arrayNode.add(objectNode);
        }
        return ResponseEntity.ok(arrayNode);
    }

    @GetMapping("/channelColors")
    public ResponseEntity<ArrayNode> getColors() {
        ArrayNode arrayNode = noJsonValueMapper.createArrayNode();
        for (Color color : Color.getNonTransparentValues()) {
            ObjectNode objectNode = noJsonValueMapper.createObjectNode()
                    .put("name", color.name())
                    .put("hex", color.getHex());
            arrayNode.add(objectNode);
        }
        return ResponseEntity.ok(arrayNode);
    }

    @Secured("ROLE_USER")
    @GetMapping("/flagReasons")
    public ResponseEntity<ArrayNode> getFlagReasons() {
        ArrayNode arrayNode = noJsonValueMapper.createArrayNode();
        for (FlagReason flagReason : FlagReason.getValues()) {
            ObjectNode objectNode = noJsonValueMapper.createObjectNode()
                    .put("type", flagReason.name())
                    .put("title", flagReason.getTitle());
            arrayNode.add(objectNode);
        }
        return ResponseEntity.ok(arrayNode);
    }

    @GetMapping("/sponsor")
    public ResponseEntity<HangarConfig.Sponsor> getSponsor() {
        return ResponseEntity.ok(config.getSponsors().get(ThreadLocalRandom.current().nextInt(config.getSponsors().size())));
    }

    @GetMapping("/announcements")
    public ResponseEntity<List<Announcement>> getAnnouncements() {
        return ResponseEntity.ok(config.getAnnouncements());
    }

    @GetMapping("/projectRoles")
    public ResponseEntity<List<ProjectRole>> getAssignableProjectRoles() {
        return ResponseEntity.ok(ProjectRole.getAssignableRoles());
    }

    @GetMapping("/orgRoles")
    public ResponseEntity<List<OrganizationRole>> getAssignableOrganizationRoles() {
        return ResponseEntity.ok(OrganizationRole.getAssignableRoles());
    }

    @GetMapping("/licences")
    public ResponseEntity<List<String>> getLicences() {
        return ResponseEntity.ok(config.getLicences());
    }

    @GetMapping("/visibilities")
    public ResponseEntity<ArrayNode> getVisibilities() {
        ArrayNode arrayNode = noJsonValueMapper.createArrayNode();
        for (Visibility value : Visibility.getValues()) {
            ObjectNode objectNode = noJsonValueMapper.createObjectNode();
            objectNode.put("name", value.getName())
                    .put("showModal", value.getShowModal())
                    .put("cssClass", value.getCssClass())
                    .put("title", value.getTitle());
            arrayNode.add(objectNode);
        }
        return ResponseEntity.ok(arrayNode);
    }

    @ResponseBody
    @GetMapping("/prompts")
    public Prompt[] getPrompts() {
        return Prompt.getValues();
    }

    @GetMapping("/validations")
    public ResponseEntity<ObjectNode> getValidations() {
        ObjectNode validations = noJsonValueMapper.createObjectNode();
        ObjectNode projectValidations = noJsonValueMapper.createObjectNode();
        projectValidations.set("name", noJsonValueMapper.valueToTree(new Validation(config.projects.getNameRegex(), config.projects.getMaxNameLen(), null)));
        projectValidations.set("desc", noJsonValueMapper.valueToTree(new Validation(null, config.projects.getMaxDescLen(), null)));
        projectValidations.set("keywords", noJsonValueMapper.valueToTree(new Validation(null, config.projects.getMaxKeywords(), null)));
        projectValidations.set("channels", noJsonValueMapper.valueToTree(new Validation(config.channels.getNameRegex(), config.channels.getMaxNameLen(), null)));
        projectValidations.set("pageName", noJsonValueMapper.valueToTree(new Validation(config.pages.getNameRegex(), config.pages.getMaxNameLen(), config.pages.getMinNameLen())));
        projectValidations.set("pageContent", noJsonValueMapper.valueToTree(new Validation(null, config.pages.getMaxLen(), config.pages.getMinLen())));
        projectValidations.put("maxPageCount", config.projects.getMaxPages());
        projectValidations.put("maxChannelCount", config.projects.getMaxChannels());
        validations.set("project", projectValidations);
        validations.set("userTagline", noJsonValueMapper.valueToTree(new Validation(null, config.user.getMaxTaglineLen(), null)));
        validations.set("version", noJsonValueMapper.valueToTree(new Validation(config.projects.getVersionNameRegex(), null, null)));
        validations.set("org", noJsonValueMapper.valueToTree(new Validation(config.org.getNameRegex(), config.org.getMaxNameLen(), config.org.getMinNameLen())));
        validations.put("maxOrgCount", config.org.getCreateLimit());
        validations.put("urlRegex", config.getUrlRegex());
        return ResponseEntity.ok(validations);
    }

    @JsonInclude(Include.NON_NULL)
    private static class Validation {

        private final String regex;
        private final Integer max;
        private final Integer min;

        public Validation(String regex, Integer max, Integer min) {
            this.regex = regex;
            this.max = max;
            this.min = min;
        }

        public String getRegex() {
            return regex;
        }

        public Integer getMax() {
            return max;
        }

        public Integer getMin() {
            return min;
        }
    }
}

