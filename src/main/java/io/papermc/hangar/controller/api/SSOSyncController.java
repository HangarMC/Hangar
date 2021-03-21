package io.papermc.hangar.controller.api;

import io.papermc.hangar.config.hangar.SsoConfig;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.internal.sso.SsoSyncData;
import io.papermc.hangar.service.internal.auth.SSOService;
import io.papermc.hangar.service.internal.users.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.constraints.NotEmpty;
import java.util.Map;

@Controller
@Api(tags = "Sessions (Authentication)", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
@RequestMapping(path = "/api", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, method = RequestMethod.POST)
public class SSOSyncController {

    private static final Logger log = LoggerFactory.getLogger(SSOSyncController.class);

    private final SSOService ssoService;
    private final UserService userService;
    private final SsoConfig ssoConfig;

    @Autowired
    public SSOSyncController(SSOService ssoService, UserService userService, SsoConfig ssoConfig) {
        this.ssoService = ssoService;
        this.userService = userService;
        this.ssoConfig = ssoConfig;
    }

    @ApiOperation(
            value = "Syncs SSO data to Hangar",
            nickname = "syncSso",
            notes = "Syncs data for a user from a SpongeAuth-compatible SSO provider. The SSO provider must be provided with this endpoint, as well as with a secret and API key that matches this Hangar instance.",
            authorizations = @Authorization("SSO"),
            tags = "Sessions (Authentication)"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 401, message = "Sent if the signature or API key missing or invalid.")
    })
    @PostMapping(value = "/sync_sso", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<MultiValueMap<String, String>> syncSso(@RequestParam @NotEmpty String sso, @RequestParam @NotEmpty String sig, @RequestParam("api_key") @NotEmpty String apiKey) {
        if (!apiKey.equals(ssoConfig.getApiKey())) {
            log.warn("SSO sync failed: bad API key (" + apiKey + " provided, " + ssoConfig.getApiKey() + " expected)");
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "SSO sync failed: bad API key (" + apiKey + " provided, " + ssoConfig.getApiKey() + " expected)");
        }

        try {
            Map<String, String> map = ssoService.decode(sso, sig);
            SsoSyncData data = SsoSyncData.fromSignedPayload(map);
            userService.ssoSyncUser(data);
            log.debug("SSO sync successful: " + map.toString());
            MultiValueMap<String, String> ssoResponse = new LinkedMultiValueMap<>();
            ssoResponse.set("status", "success");
            return ResponseEntity.ok(ssoResponse);
        } catch (SSOService.SignatureException e) {
            log.warn("SSO sync failed: invalid signature (" + sig + " for data " + sso + ")");
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "SSO sync failed: invalid signature (" + sig + " for data " + sso + ")");
        }
    }
}
