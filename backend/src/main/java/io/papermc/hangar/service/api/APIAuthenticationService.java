package io.papermc.hangar.service.api;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.db.dao.internal.table.UserDAO;
import io.papermc.hangar.db.dao.internal.table.auth.ApiKeyDAO;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.api.auth.ApiSession;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.model.db.auth.ApiKeyTable;
import io.papermc.hangar.service.TokenService;
import io.papermc.hangar.util.CryptoUtils;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class APIAuthenticationService extends HangarComponent {

    private static final int UUID_LENGTH = 36;
    private static final int API_KEY_LENGTH = UUID_LENGTH * 2 + 1;
    private final UserDAO userDAO;
    private final ApiKeyDAO apiKeyDAO;
    private final TokenService tokenService;

    @Autowired
    public APIAuthenticationService(final UserDAO userDAO, final ApiKeyDAO apiKeyDAO, final TokenService tokenService) {
        this.userDAO = userDAO;
        this.apiKeyDAO = apiKeyDAO;
        this.tokenService = tokenService;
    }

    public ApiSession createJWTForApiKey(final String apiKey) {
        if (apiKey.length() != API_KEY_LENGTH || apiKey.charAt(UUID_LENGTH) != '.') {
            throw new HangarApiException("Badly formatted API Key");
        }

        final String identifier = apiKey.substring(0, UUID_LENGTH);
        final String token = apiKey.substring(UUID_LENGTH + 1, API_KEY_LENGTH);
        // TODO Store as uuid
        final String hashedToken = CryptoUtils.hmacSha256(this.config.security.tokenSecret(), token.getBytes(StandardCharsets.UTF_8));
        final ApiKeyTable apiKeyTable = this.apiKeyDAO.findApiKey(identifier, hashedToken);
        if (apiKeyTable == null) {
            throw new HangarApiException("No valid API Key found");
        }

        final UserTable userTable = this.userDAO.getUserTable(apiKeyTable.getOwnerId());
        final String jwt = this.tokenService.expiring(userTable, apiKeyTable.getPermissions(), identifier);
        return new ApiSession(jwt, this.config.security.refreshTokenExpiry().toSeconds());
    }
}
