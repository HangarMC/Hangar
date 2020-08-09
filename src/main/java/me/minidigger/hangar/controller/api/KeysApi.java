package me.minidigger.hangar.controller.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import me.minidigger.hangar.model.generated.ApiKeyRequest;
import me.minidigger.hangar.model.generated.ApiKeyResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Api(value = "keys", description = "the keys API", tags = "Keys")
@RequestMapping("/api/v2/")
public interface KeysApi {

    @ApiOperation(value = "Creates an API key", nickname = "createKey", notes = "Creates an API key. Requires the `edit_api_keys` permission.", response = ApiKeyResponse.class, authorizations = {
            @Authorization(value = "Session")}, tags = "Keys")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok", response = ApiKeyResponse.class),
            @ApiResponse(code = 401, message = "Api session missing, invalid or expired"),
            @ApiResponse(code = 403, message = "Not enough permissions to use this endpoint")})
    @PostMapping(value = "/keys",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiKeyResponse> createKey(@ApiParam(value = "", required = true) @Valid @RequestBody ApiKeyRequest body);


    @ApiOperation(value = "Delete an API key", nickname = "deleteKey", notes = "Delete an API key. Requires the `edit_api_keys` permission.", authorizations = {
            @Authorization(value = "Session")}, tags = "Keys")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Key deleted"),
            @ApiResponse(code = 401, message = "Api session missing, invalid or expired"),
            @ApiResponse(code = 403, message = "Not enough permissions to use this endpoint")})
    @DeleteMapping(value = "/keys")
    ResponseEntity<Void> deleteKey(@NotNull @ApiParam(value = "The name of the key to delete", required = true) @Valid @RequestParam("name") String name);

}
