package io.papermc.hangar.service.api;

import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.dao.internal.table.UserDAO;
import io.papermc.hangar.db.dao.internal.table.auth.ApiKeyDAO;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.api.auth.ApiSession;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.model.db.auth.ApiKeyTable;
import io.papermc.hangar.service.HangarService;
import io.papermc.hangar.service.PermissionService;
import io.papermc.hangar.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class APIAuthenticationService extends HangarService {

    private static final String UUID_REGEX = "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}";
    private static final Pattern API_KEY_PATTERN = Pattern.compile("(" + UUID_REGEX + ").(" + UUID_REGEX + ")");

    private final UserDAO userDAO;
    private final ApiKeyDAO apiKeyDAO;
    private final TokenService tokenService;
    private final PermissionService permissionService;

    @Autowired
    public APIAuthenticationService(HangarDao<UserDAO> userDAO, HangarDao<ApiKeyDAO> apiKeyDAO, TokenService tokenService, PermissionService permissionService) {
        this.userDAO = userDAO.get();
        this.apiKeyDAO = apiKeyDAO.get();
        this.tokenService = tokenService;
        this.permissionService = permissionService;
    }

    public ApiSession createJWTForApiKey(String apiKey) {
        if (!API_KEY_PATTERN.matcher(apiKey).matches()) {
            throw new HangarApiException("Badly formatted API Key");
        }
        String identifier = apiKey.split("\\.")[0];
        String token = apiKey.split("\\.")[1];
        ApiKeyTable apiKeyTable = apiKeyDAO.findApiKey(identifier, token);
        if (apiKeyTable == null) {
            throw new HangarApiException("No valid API Key found");
        }
        UserTable userTable = userDAO.getUserTable(apiKeyTable.getOwnerId());
        String jwt = tokenService.expiring(userTable, permissionService.getGlobalPermissions(userTable.getId()), identifier);
        return new ApiSession(jwt, config.security.getRefreshTokenExpiry().toSeconds());
    }
    // 006ad884-3df9-43e8-af01-91590f92cfd7.fa31831d-097f-4d11-9031-b57b41c59fa1
}
