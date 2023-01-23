package io.papermc.hangar.controller.api.v1.interfaces;

import io.papermc.hangar.model.api.ApiKey;
import io.papermc.hangar.model.internal.api.requests.CreateAPIKeyForm;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "API Keys")
@RequestMapping("/api/v1")
public interface IApiKeysController {

    @Operation(
        summary = "Creates an API key",
        operationId = "createKey",
        description = "Creates an API key. Requires the `edit_api_keys` permission.",
        security = @SecurityRequirement(name = "HangarAuth", scopes = "edit_api_keys"),
        tags = "API Keys"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Key created", content = @Content(schema = @Schema(implementation = String.class))),
        @ApiResponse(responseCode = "401", description = "Api session missing, invalid or expired"),
        @ApiResponse(responseCode = "403", description = "Not enough permissions to use this endpoint")})
    @PostMapping(path = "/keys", produces = MediaType.TEXT_PLAIN_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    String createKey(@Parameter(description = "Data about the key to create", required = true) @Valid @RequestBody CreateAPIKeyForm apiKeyForm);

    @Operation(
        summary = "Fetches a list of API Keys",
        operationId = "getKeys",
        description = "Fetches a list of API Keys. Requires the `edit_api_keys` permission.",
        security = @SecurityRequirement(name = "HangarAuth", scopes = "edit_api_keys"),
        tags = "API Keys"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Key created", content = @Content(schema = @Schema(implementation = ApiKey.class))),
        @ApiResponse(responseCode = "401", description = "Api session missing, invalid or expired"),
        @ApiResponse(responseCode = "403", description = "Not enough permissions to use this endpoint")})
    @GetMapping(path = "/keys", produces = MediaType.APPLICATION_JSON_VALUE)
    List<ApiKey> getKeys();

    @Operation(
        summary = "Deletes an API key",
        operationId = "deleteKey",
        description = "Deletes an API key. Requires the `edit_api_keys` permission.",
        security = @SecurityRequirement(name = "HangarAuth", scopes = "edit_api_keys"),
        tags = "API Keys"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Key deleted"),
        @ApiResponse(responseCode = "401", description = "Api session missing, invalid or expired"),
        @ApiResponse(responseCode = "403", description = "Not enough permissions to use this endpoint")
    })
    @DeleteMapping("/keys")
    void deleteKey(@Parameter(description = "The name of the key to delete", required = true) @RequestParam String name);
}
