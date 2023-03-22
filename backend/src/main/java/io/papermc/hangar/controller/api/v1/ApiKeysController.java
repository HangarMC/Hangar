package io.papermc.hangar.controller.api.v1;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.controller.api.v1.interfaces.IApiKeysController;
import io.papermc.hangar.model.api.ApiKey;
import io.papermc.hangar.model.common.NamedPermission;
import io.papermc.hangar.model.internal.api.requests.CreateAPIKeyForm;
import io.papermc.hangar.security.annotations.permission.PermissionRequired;
import io.papermc.hangar.security.annotations.ratelimit.RateLimit;
import io.papermc.hangar.security.annotations.unlocked.Unlocked;
import io.papermc.hangar.service.APIKeyService;
import io.papermc.hangar.service.PermissionService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@RateLimit(path = "apikeys", greedy = true)
@PermissionRequired(NamedPermission.EDIT_API_KEYS)
public class ApiKeysController extends HangarComponent implements IApiKeysController {

    private final APIKeyService apiKeyService;
    private final PermissionService permissionService;

    @Autowired
    public ApiKeysController(final APIKeyService apiKeyService, final PermissionService permissionService) {
        this.apiKeyService = apiKeyService;
        this.permissionService = permissionService;
    }

    @Override
    @ResponseBody
    @Unlocked
    @RateLimit(overdraft = 5, refillTokens = 1, refillSeconds = 15)
    @ResponseStatus(HttpStatus.CREATED)
    public String createKey(final CreateAPIKeyForm apiKeyForm) {
        return this.apiKeyService.createApiKey(this.getHangarPrincipal(), apiKeyForm, this.permissionService.getAllPossiblePermissions(this.getHangarPrincipal().getUserId()));
    }

    @Override
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public List<ApiKey> getKeys() {
        return this.apiKeyService.getApiKeys(this.getHangarPrincipal().getUserId());
    }

    @Override
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteKey(final String name) {
        this.apiKeyService.deleteApiKey(this.getHangarPrincipal(), name);
    }
}
