package io.papermc.hangar.controller.api;

import io.papermc.hangar.db.customtypes.LoggedActionType;
import io.papermc.hangar.db.customtypes.LoggedActionType.UserContext;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.ApiAuthInfo;
import io.papermc.hangar.model.NamedPermission;
import io.papermc.hangar.model.Permission;
import io.papermc.hangar.model.generated.ApiKeyRequest;
import io.papermc.hangar.model.generated.ApiKeyResponse;
import io.papermc.hangar.service.ApiKeyService;
import io.papermc.hangar.service.UserActionLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApiController
@Controller
public class KeysApiController implements KeysApi {

    private final ApiKeyService apiKeyService;
    private final UserActionLogService userActionLogService;

    private final ApiAuthInfo apiAuthInfo;
    private final HttpServletRequest request;

    @Autowired
    public KeysApiController(ApiKeyService apiKeyService, UserActionLogService userActionLogService, ApiAuthInfo apiAuthInfo, HttpServletRequest request) {
        this.apiKeyService = apiKeyService;
        this.userActionLogService = userActionLogService;
        this.apiAuthInfo = apiAuthInfo;
        this.request = request;
    }

    @Override
    @PreAuthorize("@authenticationService.authApiRequest(T(io.papermc.hangar.model.Permission).EditApiKeys, T(io.papermc.hangar.controller.util.ApiScope).forGlobal())")
    public ResponseEntity<ApiKeyResponse> createKey(ApiKeyRequest body) {
        List<NamedPermission> perms;
        try {
            perms = NamedPermission.parseNamed(body.getPermissions());
        } catch (IllegalArgumentException e) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "Invalid permission name");
        }
        if (perms.isEmpty()) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "Must include one permission");
        }
        if (body.getName().length() > 255) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "Name too long");
        }
        Permission perm = perms.stream().map(NamedPermission::getPermission).reduce(Permission::add).orElse(Permission.None);
        boolean isSubKey = apiAuthInfo.getKey() == null || apiAuthInfo.getKey().isSubKey(perm);
        if (!isSubKey) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "Not enough permissions to create that key");
        }
        String tokenIdentifier = UUID.randomUUID().toString();
        String token = UUID.randomUUID().toString();
        long userId = apiAuthInfo.getUser().getId();

        if (apiKeyService.getKey(body.getName(), userId) != null) {
            throw new HangarApiException(HttpStatus.CONFLICT, "Name already taken");
        }

        apiKeyService.createApiKey(body.getName(), userId, tokenIdentifier, token, perm);
        userActionLogService.user(request, LoggedActionType.USER_APIKEY_CREATE.with(UserContext.of(apiAuthInfo.getUserId())), "Key Name: " + body.getName() + "<br>" + perms.stream().map(NamedPermission::getFrontendName).collect(Collectors.joining(",<br>")), "");
        return ResponseEntity.ok(new ApiKeyResponse().perms(perm.toNamed()).key(tokenIdentifier + "." + token));
    }

    @Override
    @PreAuthorize("@authenticationService.authApiRequest(T(io.papermc.hangar.model.Permission).EditApiKeys, T(io.papermc.hangar.controller.util.ApiScope).forGlobal())")
    public ResponseEntity<Void> deleteKey(String name) {
        if (apiAuthInfo.getUser() == null) {
            throw new HangarApiException(HttpStatus.BAD_REQUEST, "Public keys can't be used to delete");
        }
        int rowsAffected = apiKeyService.deleteApiKey(name, apiAuthInfo.getUser().getId());
        if (rowsAffected == 0) {
            throw new HangarApiException(HttpStatus.NOT_FOUND);
        } else {
            userActionLogService.user(request, LoggedActionType.USER_APIKEY_DELETE.with(UserContext.of(apiAuthInfo.getUserId())), "", "Key Name: " + name);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }
}
