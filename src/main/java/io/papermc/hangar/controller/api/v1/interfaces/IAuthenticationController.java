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
import org.springframework.web.bind.annotation.RequestParam;

@Api(tags = "Authentication", produces = MediaType.APPLICATION_JSON_VALUE)
@RequestMapping(path = "/api/v1", produces = MediaType.APPLICATION_JSON_VALUE)
public interface IAuthenticationController {

    @ApiOperation(
            value = "Creates an API JWT",
            nickname = "authenticate",
            notes = "`Log-in` with your API key in order to be able to call other endpoints authenticated. The returned JWT should be specified as a header in all following requests: `Authorization: HangarAuth your.jwt`",
            authorizations = @Authorization("Key"),
            tags = "Authentication"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 401, message = "Api key missing or invalid")
    })
    @PostMapping("/authenticate")
    ResponseEntity<ApiSession> authenticate(@ApiParam("JWT") @RequestParam String apiKey);
}
