package io.papermc.hangar.controllerold.extras.exceptions.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.papermc.hangar.controllerold.HangarController;
import io.papermc.hangar.controllerold.extras.exceptions.JsonResponseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Deprecated
@ControllerAdvice(assignableTypes = { HangarController.class })
public class JsonResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    private final ObjectMapper mapper;

    @Autowired
    public JsonResponseEntityExceptionHandler(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @ExceptionHandler(JsonResponseException.class)
    public ResponseEntity<ObjectNode> handleException(JsonResponseException exception) {
        ObjectNode objectNode = mapper.createObjectNode()
                .put("status", exception.getStatus().value())
                .put("messageKey", exception.getMessageKey())
                .set("messageArgs", mapper.valueToTree(exception.getArgs()));
        return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).body(objectNode);
    }
}
