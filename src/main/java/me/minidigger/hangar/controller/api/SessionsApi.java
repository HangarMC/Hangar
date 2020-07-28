package me.minidigger.hangar.controller.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;

@Api(value = "sessions", description = "the sessions API", tags = "Sessions (Authentication)")
@RequestMapping("/api/v2/")
public interface SessionsApi {

    @ApiOperation(value = "Invalidates the API session used for the request.", nickname = "deleteSession", notes = "Invalidates the API session used to make this call.", authorizations = {
            @Authorization(value = "Session")}, tags = {"Sessions (Authentication)",})
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Session invalidated"),
            @ApiResponse(code = 400, message = "Sent if this request was not made with a session."),
            @ApiResponse(code = 401, message = "Api session missing, invalid or expired"),
            @ApiResponse(code = 403, message = "Not enough permissions to use this endpoint")})
    @RequestMapping(value = "/sessions/current",
            method = RequestMethod.DELETE)
    ResponseEntity<Void> deleteSession();

}
