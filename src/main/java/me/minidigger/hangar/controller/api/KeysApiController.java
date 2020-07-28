package me.minidigger.hangar.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;

import me.minidigger.hangar.model.generated.ApiKeyRequest;
import me.minidigger.hangar.model.generated.ApiKeyResponse;

@Controller
public class KeysApiController implements KeysApi {

    private static final Logger log = LoggerFactory.getLogger(KeysApiController.class);

    private final ObjectMapper objectMapper;

    private final HttpServletRequest request;

    @Autowired
    public KeysApiController(ObjectMapper objectMapper, HttpServletRequest request) {
        this.objectMapper = objectMapper;
        this.request = request;
    }

    @Override
    public ResponseEntity<ApiKeyResponse> createKey(ApiKeyRequest body) {
        try {
            return new ResponseEntity<>(objectMapper.readValue("{\n  \"perms\" : [ \"view_public_info\", \"view_public_info\" ],\n  \"key\" : \"key\"\n}", ApiKeyResponse.class), HttpStatus.OK); // TODO Implement me
        } catch (IOException e) {
            log.error("Couldn't serialize response for content type application/json", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<Void> deleteKey(String name) {
        String accept = request.getHeader("Accept");
        return new ResponseEntity<>(HttpStatus.OK); // TODO Implement me
    }

}
