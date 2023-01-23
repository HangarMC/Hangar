package io.papermc.hangar.controller.api.v1.interfaces;

import io.papermc.hangar.model.api.auth.ApiSession;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Authentication")
@RequestMapping(path = "/api/v1", produces = MediaType.APPLICATION_JSON_VALUE)
public interface IAuthenticationController {

    @Operation(
        summary = "Creates an API JWT",
        operationId = "authenticate",
        description = "`Log-in` with your API key in order to be able to call other endpoints authenticated. The returned JWT should be specified as a header in all following requests: `Authorization: HangarAuth your.jwt`",
        tags = "Authentication"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ok"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "401", description = "Api key missing or invalid")
    })
    @PostMapping("/authenticate")
    ResponseEntity<ApiSession> authenticate(@Parameter(description = "JWT") @RequestParam String apiKey);
}
