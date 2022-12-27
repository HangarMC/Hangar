package io.papermc.hangar.controller.api.v1.interfaces;

import io.papermc.hangar.model.api.ApiKey;
import io.papermc.hangar.model.internal.api.requests.CreateAPIKeyForm;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import javax.validation.Valid;

@Api(tags = "API Keys")
@RequestMapping("/api/v1")
public interface IApiKeysController {

    @ApiOperation(
            value = "Creates an API key",
            nickname = "createKey",
            notes = "Creates an API key. Requires the `edit_api_keys` permission.",
            response = String.class,
            authorizations = @Authorization("Session"),
            tags = "API Keys"
    )
    @ApiResponses({
            @ApiResponse(code = 201, message = "Key created", response = String.class),
            @ApiResponse(code = 401, message = "Api session missing, invalid or expired"),
            @ApiResponse(code = 403, message = "Not enough permissions to use this endpoint")})
    @PostMapping(path = "/keys", produces = MediaType.TEXT_PLAIN_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    String createKey(@ApiParam(value = "Data about the key to create", required = true) @Valid @RequestBody CreateAPIKeyForm apiKeyForm);

    @ApiOperation(
            value = "Fetches a list of API Keys",
            nickname = "getKeys",
            notes = "Fetches a list of API Keys. Requires the `edit_api_keys` permission.",
            response = String.class,
            authorizations = @Authorization("Session"),
            tags = "API Keys"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Key created", response = ApiKey.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Api session missing, invalid or expired"),
            @ApiResponse(code = 403, message = "Not enough permissions to use this endpoint")})
    @GetMapping(path = "/keys", produces = MediaType.APPLICATION_JSON_VALUE)
    List<ApiKey> getKeys();

    @ApiOperation(
            value = "Deletes an API key",
            nickname = "deleteKey",
            notes = "Deletes an API key. Requires the `edit_api_keys` permission.",
            authorizations = @Authorization("Session"),
            tags = "API Keys"
    )
    @ApiResponses({
            @ApiResponse(code = 204, message = "Key deleted"),
            @ApiResponse(code = 401, message = "Api session missing, invalid or expired"),
            @ApiResponse(code = 403, message = "Not enough permissions to use this endpoint")
    })
    @DeleteMapping("/keys")
    void deleteKey(@ApiParam(value = "The name of the key to delete", required = true) @RequestParam String name);
}
