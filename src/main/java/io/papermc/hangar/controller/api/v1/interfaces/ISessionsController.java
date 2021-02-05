package io.papermc.hangar.controller.api.v1.interfaces;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Api(tags = "Sessions (Authentication)")
@RequestMapping(path = "/api/v1", method = RequestMethod.DELETE)
public interface ISessionsController {

    @ApiOperation(
            value = "Invalidates the API session used for the request.",
            nickname = "deleteSession",
            notes = "Invalidates the API session used to make this call.",
            authorizations = @Authorization(value = "Session"),
            tags = "Sessions (Authentication)")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Session invalidated"),
            @ApiResponse(code = 400, message = "Sent if this request was not made with a session."),
            @ApiResponse(code = 401, message = "Api session missing, invalid or expired"),
            @ApiResponse(code = 403, message = "Not enough permissions to use this endpoint")})
    @DeleteMapping("/sessions/current")
    @PreAuthorize("@authenticationService.handleApiRequest(T(io.papermc.hangar.model.common.Permission).None, T(io.papermc.hangar.controller.extras.ApiScope).ofGlobal())")
    ResponseEntity<Void> deleteSession();
}
