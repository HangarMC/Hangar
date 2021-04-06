package io.papermc.hangar.controller.api.v1.interfaces;

import io.papermc.hangar.model.api.auth.ApiSession;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Api(tags = "Sessions (Authentication)", produces = MediaType.APPLICATION_JSON_VALUE)
@RequestMapping(path = "/api/v1", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
public interface IAuthenticationController {

    @ApiOperation(
            value = "Creates an API JWT",
            nickname = "authenticate",
            notes = "Creates a new API JWT. Pass an API key to create an authenticated session. When passing an API key, you should use the scheme `HangarAuth`, and parameter `apikey`. An example would be `Authorization: HangarAuth <apikey>`. The returned JWT should be specified in all following request as the parameter `token`.",
            authorizations = @Authorization(value = "Key"),
            tags = "Sessions (Authentication)"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 401, message = "Api key missing or invalid")
    })
    @PostMapping("/authenticate")
    ResponseEntity<ApiSession> authenticate(@ApiParam("JWT") @RequestParam String apiKey);
}
