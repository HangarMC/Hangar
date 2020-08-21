package io.papermc.hangar.controller.api;

import io.swagger.annotations.*;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.constraints.NotEmpty;

@Api(value = "sso", description = "the SSO API", tags = "Sessions (Authentication)")
@RequestMapping("/api/v2/")
public interface SsoApi {

    @ApiOperation(value = "Syncs SSO data to Hangar", nickname = "syncSso", notes = "Syncs data for a user from a SpongeAuth-compatible SSO provider. The SSO provider must be provided with this endpoint, as well as with a secret and API key that matches this Hangar instance.", authorizations = {
            @Authorization(value = "SSO")}, tags = "SSO")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 401, message = "Sent if the signature or API key missing or invalid.")})
    @PostMapping(value = "/sync_sso",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    ResponseEntity<Void> syncSso(@NotEmpty String sso, @NotEmpty String sig, @NotEmpty String apiKey);

}
