package io.papermc.hangar.controller.internal;

import io.papermc.hangar.model.api.ApiKey;
import io.papermc.hangar.model.common.NamedPermission;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.model.internal.api.requests.CreateAPIKeyForm;
import io.papermc.hangar.model.internal.api.requests.StringContent;
import io.papermc.hangar.security.annotations.LoggedIn;
import io.papermc.hangar.security.annotations.currentuser.CurrentUser;
import io.papermc.hangar.security.annotations.permission.PermissionRequired;
import io.papermc.hangar.security.annotations.ratelimit.RateLimit;
import io.papermc.hangar.service.APIKeyService;
import io.papermc.hangar.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.validation.Valid;
import java.util.List;

@LoggedIn
@Controller
@RateLimit(path = "apikey")
@RequestMapping("/api/internal/api-keys")
@PermissionRequired(NamedPermission.EDIT_API_KEYS)
public class ApiKeyController {

    private final PermissionService permissionService;
    private final APIKeyService apiKeyService;

    @Autowired
    public ApiKeyController(PermissionService permissionService, APIKeyService apiKeyService) {
        this.permissionService = permissionService;
        this.apiKeyService = apiKeyService;
    }

    @ResponseStatus(HttpStatus.OK)
    @CurrentUser("#user")
    @GetMapping("/check-key/{user}")
    public void checkKeyName(@PathVariable UserTable user, @RequestParam String name) {
        apiKeyService.checkName(user, name);
    }

    @ResponseBody
    @CurrentUser("#user")
    @GetMapping(path = "/existing-keys/{user}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ApiKey> getApiKeys(@PathVariable UserTable user) {
        return apiKeyService.getApiKeys(user.getId());
    }

    @ResponseBody
    @CurrentUser("#user")
    @GetMapping(path = "/possible-perms/{user}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<NamedPermission> getPossiblePermissions(@PathVariable UserTable user) {
        return permissionService.getAllPossiblePermissions(user.getId()).toNamed();
    }

    @ResponseBody
    @CurrentUser("#user")
    @RateLimit(overdraft = 5, refillTokens = 1, refillSeconds = 20)
    @PostMapping(path = "/create-key/{user}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public String createApiKey(@PathVariable UserTable user, @RequestBody @Valid CreateAPIKeyForm apiKeyForm) {
        return apiKeyService.createApiKey(user, apiKeyForm, permissionService.getAllPossiblePermissions(user.getId()));
    }

    @ResponseBody
    @CurrentUser("#user")
    @PostMapping(path = "/delete-key/{user}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void deleteApiKey(@PathVariable UserTable user, @RequestBody @Valid StringContent nameContent) {
        apiKeyService.deleteApiKey(user, nameContent.getContent());
    }
}
