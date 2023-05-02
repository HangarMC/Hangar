package io.papermc.hangar.service;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.db.dao.internal.HangarApiKeysDAO;
import io.papermc.hangar.db.dao.internal.table.auth.ApiKeyDAO;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.api.ApiKey;
import io.papermc.hangar.model.common.NamedPermission;
import io.papermc.hangar.model.common.Permission;
import io.papermc.hangar.model.db.auth.ApiKeyTable;
import io.papermc.hangar.model.identified.UserIdentified;
import io.papermc.hangar.model.internal.api.requests.CreateAPIKeyForm;
import io.papermc.hangar.model.internal.logs.LogAction;
import io.papermc.hangar.model.internal.logs.contexts.UserContext;
import io.papermc.hangar.util.CryptoUtils;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class APIKeyService extends HangarComponent {

    private final ApiKeyDAO apiKeyDAO;
    private final HangarApiKeysDAO hangarApiKeysDAO;

    @Autowired
    public APIKeyService(final ApiKeyDAO apiKeyDAO, final HangarApiKeysDAO hangarApiKeysDAO) {
        this.apiKeyDAO = apiKeyDAO;
        this.hangarApiKeysDAO = hangarApiKeysDAO;
    }

    public List<ApiKey> getApiKeys(final long userId) {
        return this.hangarApiKeysDAO.getUserApiKeys(userId);
    }

    public void checkName(final UserIdentified userIdentified, final String name) {
        if (this.apiKeyDAO.getByUserAndName(userIdentified.getUserId(), name) != null) {
            throw new HangarApiException("apiKeys.error.duplicateName");
        }
    }

    @Transactional
    public String createApiKey(final UserIdentified userIdentified, final CreateAPIKeyForm apiKeyForm, final Permission possiblePermissions) {
        final Permission keyPermission = apiKeyForm.permissions().stream().map(NamedPermission::getPermission).reduce(Permission::add).orElse(Permission.None);
        if (!possiblePermissions.has(keyPermission)) {
            throw new HangarApiException("apiKeys.error.notEnoughPerms");
        }

        this.checkName(userIdentified, apiKeyForm.name());

        final UUID tokenIdentifier = UUID.randomUUID();
        final String token = UUID.randomUUID().toString();
        final String hashedToken = CryptoUtils.hmacSha256(this.config.security.tokenSecret(), token.getBytes(StandardCharsets.UTF_8));
        this.apiKeyDAO.insert(new ApiKeyTable(apiKeyForm.name(), userIdentified.getUserId(), tokenIdentifier, hashedToken, keyPermission));
        this.actionLogger.user(LogAction.USER_APIKEY_CREATED.create(UserContext.of(userIdentified.getUserId()), "Key '" + apiKeyForm.name() + "': " + apiKeyForm.permissions().stream().map(NamedPermission::getFrontendName).collect(Collectors.joining(", ")), ""));
        return tokenIdentifier + "." + token;
    }

    @Transactional
    public void deleteApiKey(final UserIdentified userIdentified, final String keyName) {
        if (this.apiKeyDAO.delete(keyName, userIdentified.getUserId()) == 0) {
            throw new HangarApiException(HttpStatus.NOT_FOUND);
        }
        this.actionLogger.user(LogAction.USER_APIKEY_DELETED.create(UserContext.of(userIdentified.getUserId()), "", "Key '" + keyName + "'"));
    }

}
