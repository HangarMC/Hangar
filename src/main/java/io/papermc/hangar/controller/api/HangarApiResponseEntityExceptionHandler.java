package io.papermc.hangar.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.papermc.hangar.exceptions.HangarApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice(annotations = {ApiController.class})
public class HangarApiResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    private final ObjectMapper mapper;

    @Autowired
    public HangarApiResponseEntityExceptionHandler(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @ExceptionHandler(HangarApiException.class)
    public ResponseEntity<ObjectNode> handleException(HangarApiException exception, HttpServletRequest request) {
        ObjectNode response = mapper.createObjectNode()
                .put("message", exception.getReason());
        ObjectNode error = mapper.createObjectNode()
                .put("message", exception.getStatus().getReasonPhrase())
                .put("code", exception.getStatus().value());
        response.set("error", error);
        response.set("path_params", mapper.valueToTree(request.getAttribute(View.PATH_VARIABLES)));
        return new ResponseEntity<>(response, exception.getResponseHeaders(), exception.getStatus());
    }
}
