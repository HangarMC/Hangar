package me.minidigger.hangar.controller.api;

import io.swagger.annotations.*;
import me.minidigger.hangar.model.generated.SsoSyncSignedPayload;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;

@Api(value = "sso", description = "the SSO API", tags = "Sessions (Authentication)")
@RequestMapping("/api/v2/")
public interface SsoApi {

    // TODO: do we even want to document this in swagger?
    @ApiOperation(value = "Syncs SSO data to Hangar", nickname = "syncSso", notes = "Syncs data for a user from a SpongeAuth-compatible SSO provider. The SSO provider must be provided with this endpoint, as well as with a secret and API key that matches this Hangar instance.", authorizations = {
            @Authorization(value = "SSO")}, tags = "SSO")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 401, message = "Sent if the signature or API key missing or invalid.")})
    @PostMapping(value = "/sync_sso",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    ResponseEntity<Void> syncSso(@Valid SsoSyncSignedPayload payload);

}
