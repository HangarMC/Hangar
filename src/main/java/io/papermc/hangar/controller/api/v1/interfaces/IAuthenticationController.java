package io.papermc.hangar.controller.api.v1.interfaces;

import io.papermc.hangar.model.api.auth.ApiSession;
import io.papermc.hangar.model.api.requests.SessionProperties;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Api(tags = "Sessions (Authentication)", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
@RequestMapping(path = "/api/v1", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
public interface IAuthenticationController {

    @ApiOperation(
            value = "Creates an API session",
            nickname = "authenticate",
            notes = "Creates a new API session. Pass an API key to create an authenticated session. To create a public session, don't pass an Authorization header. When passing an API key, you should use the scheme `HangarApi`, and parameter `apikey`. An example would be `Authorization: HangarApi apikey=\"foobar\"`. The returned session should be specified in all following request as the parameter `session`. An example would be `Authorization: HangarApi session=\"noisses\"`",
            authorizations = @Authorization(value = "Key"),
            tags = "Sessions (Authentication)"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 400, message = "Sent if the requested expiration can't be used."),
            @ApiResponse(code = 401, message = "Api key missing or invalid")
    })
    @PostMapping("/authenticate")
    ResponseEntity<ApiSession> authenticate(@ApiParam("Session properties") @RequestBody(required = false) SessionProperties body);
}
