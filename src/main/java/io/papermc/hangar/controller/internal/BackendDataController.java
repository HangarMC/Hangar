package io.papermc.hangar.controller.internal;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.papermc.hangar.model.common.Color;
import io.papermc.hangar.model.common.projects.Category;
import io.papermc.hangar.model.common.projects.FlagReason;
import io.papermc.hangar.modelold.NamedPermission;
import io.papermc.hangar.security.annotations.Anyone;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.lang.annotation.Annotation;

@Controller
@Anyone
@RequestMapping(path = "/api/internal/data", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
public class BackendDataController {

    private final ObjectMapper mapper;

    @Autowired
    public BackendDataController(ObjectMapper mapper) {
        this.mapper = mapper.copy();
        this.mapper.setAnnotationIntrospector(new JacksonAnnotationIntrospector() {
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
        return ResponseEntity.ok(mapper.valueToTree(Category.getValues()));
    }

    @GetMapping("/permissions")
    public ResponseEntity<ArrayNode> getPermissions() {
        ArrayNode arrayNode = mapper.createArrayNode();
        for (NamedPermission namedPermission : NamedPermission.getValues()) {
            ObjectNode namedPermissionObject = mapper.createObjectNode();
            namedPermissionObject.put("value", namedPermission.getValue());
            namedPermissionObject.put("frontendName", namedPermission.getFrontendName());
            namedPermissionObject.put("permission", namedPermission.getPermission().toBinString());
            arrayNode.add(namedPermissionObject);
        }
        return ResponseEntity.ok(arrayNode);
    }

    @GetMapping("/platforms")
    // TODO include valid versions (equivalent of PlatformInfo)
    public ResponseEntity<ObjectNode> getPlatforms() {
        throw new NotImplementedException("NOT IMPLEMENTED");
    }

    @GetMapping("/channelColors")
    public ResponseEntity<ArrayNode> getColors() {
        ArrayNode arrayNode = mapper.createArrayNode();
        for (Color color : Color.getNonTransparentValues()) {
            ObjectNode objectNode = mapper.createObjectNode()
                    .put("name", color.name())
                    .put("hex", color.getHex());
            arrayNode.add(objectNode);
        }
        return ResponseEntity.ok(arrayNode);
    }

    @Secured("ROLE_USER")
    @GetMapping("/flagReasons")
    public ResponseEntity<ArrayNode> getFlagReasons() {
        ArrayNode arrayNode = mapper.createArrayNode();
        for (FlagReason flagReason : FlagReason.getValues()) {
            ObjectNode objectNode = mapper.createObjectNode()
                    .put("type", flagReason.name())
                    .put("title", flagReason.getTitle());
            arrayNode.add(objectNode);
        }
        return ResponseEntity.ok(arrayNode);
    }
}

