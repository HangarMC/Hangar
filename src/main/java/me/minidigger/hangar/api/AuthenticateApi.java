package me.minidigger.hangar.api;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import me.minidigger.hangar.model.SessionProperties;
import me.minidigger.hangar.model.ApiSessionResponse;

@Api(value = "authenticate", description = "the authenticate API", tags = "Sessions (Authentication)")
public interface AuthenticateApi {

    @ApiOperation(value = "Creates an API session", nickname = "authenticate", notes = "Creates a new API session. Pass an API key to create an authenticated session. To create a public session, don't pass an Authorization header. When passing an API key, you should use the scheme `HangarApi`, and parameter `apikey`. An example would be `Authorization: HangarApi apikey=\"foobar\"`. The returned session should be specified in all following request as the parameter `session`. An example would be `Authorization: HangarApi session=\"noisses\"`", response = ApiSessionResponse.class, authorizations = {
            @Authorization(value = "Key")}, tags = "Sessions (Authentication)")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok", response = ApiSessionResponse.class),
            @ApiResponse(code = 400, message = "Sent if the requested expiration can't be used."),
            @ApiResponse(code = 401, message = "Api key missing or invalid")})
    @PostMapping(value = "/authenticate",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiSessionResponse> authenticate(@ApiParam(value = "") @Valid @RequestBody SessionProperties body
    );

    @ApiOperation(value = "authenticateUser", hidden = true)
    @PostMapping(value = "/authenticate/user",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiSessionResponse> authenticateUser(@ApiParam(value = "") @Valid @RequestBody SessionProperties body
    );
}
