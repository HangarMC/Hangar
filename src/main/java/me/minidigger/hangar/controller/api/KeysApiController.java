package me.minidigger.hangar.controller.api;

import me.minidigger.hangar.model.ApiAuthInfo;
import me.minidigger.hangar.model.NamedPermission;
import me.minidigger.hangar.model.Permission;
import me.minidigger.hangar.model.generated.ApiKeyRequest;
import me.minidigger.hangar.model.generated.ApiKeyResponse;
import me.minidigger.hangar.service.ApiKeyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Controller
public class KeysApiController implements KeysApi {

    private final ApiKeyService apiKeyService;

    private final ApiAuthInfo apiAuthInfo;

    @Autowired
    public KeysApiController(ApiKeyService apiKeyService, ApiAuthInfo apiAuthInfo) {
        this.apiKeyService = apiKeyService;
        this.apiAuthInfo = apiAuthInfo;
    }

    @Override
    @PreAuthorize("@authenticationService.authApiRequest(T(me.minidigger.hangar.model.Permission).EditApiKeys, T(me.minidigger.hangar.controller.util.ApiScope).forGlobal())")
    public ResponseEntity<ApiKeyResponse> createKey(ApiKeyRequest body) {
        List<NamedPermission> perms;
        try {
            perms = NamedPermission.parseNamed(body.getPermissions());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid permission name");
        }
        if (perms.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Must include one permission");
        }
        if (body.getName().length() > 255) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name too long");
        }
        Permission perm = perms.stream().map(NamedPermission::getPermission).reduce(Permission::add).orElse(Permission.None);
        boolean isSubKey = apiAuthInfo.getKey() == null || apiAuthInfo.getKey().isSubKey(perm);
        if (!isSubKey) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not enough permissions to create that key");
        }
        String tokenIdentifier = UUID.randomUUID().toString();
        String token = UUID.randomUUID().toString();
        long userId = apiAuthInfo.getUser().getId();

        if (apiKeyService.getKey(body.getName(), userId) != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Name already taken");
        }

        apiKeyService.createApiKey(body.getName(), userId, tokenIdentifier, token, perm);
        return ResponseEntity.ok(new ApiKeyResponse().perms(perm.toNamed()).key(tokenIdentifier + "." + token));
    }

    @Override
    @PreAuthorize("@authenticationService.authApiRequest(T(me.minidigger.hangar.model.Permission).EditApiKeys, T(me.minidigger.hangar.controller.util.ApiScope).forGlobal())")
    public ResponseEntity<Void> deleteKey(String name) {
        if (apiAuthInfo.getUser() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Public keys can't be used to delete");
        }
        int rowsAffected = apiKeyService.deleteApiKey(name, apiAuthInfo.getUser().getId());
        if (rowsAffected == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }
}
