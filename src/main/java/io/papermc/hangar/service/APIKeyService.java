package io.papermc.hangar.service;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.db.dao.HangarDao;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class APIKeyService extends HangarComponent {

    private final ApiKeyDAO apiKeyDAO;
    private final HangarApiKeysDAO hangarApiKeysDAO;

    @Autowired
    public APIKeyService(HangarDao<ApiKeyDAO> apiKeyDAO, HangarDao<HangarApiKeysDAO> hangarApiKeysDAO) {
        this.apiKeyDAO = apiKeyDAO.get();
        this.hangarApiKeysDAO = hangarApiKeysDAO.get();
    }

    public List<ApiKey> getApiKeys(long userId) {
        return hangarApiKeysDAO.getUserApiKeys(userId);
    }

    public void checkName(UserIdentified userIdentified, String name) {
        if (apiKeyDAO.getByUserAndName(userIdentified.getUserId(), name) != null) {
            throw new HangarApiException("apiKeys.error.duplicateName");
        }
    }

    @Transactional
    public String createApiKey(UserIdentified userIdentified, CreateAPIKeyForm apiKeyForm, Permission possiblePermissions) {
        Permission keyPermission = apiKeyForm.getPermissions().stream().map(NamedPermission::getPermission).reduce(Permission::add).orElse(Permission.None);
        if (!possiblePermissions.has(keyPermission)) {
            throw new HangarApiException("apiKeys.error.notEnoughPerms");
        }

        checkName(userIdentified, apiKeyForm.getName());

        String tokenIdentifier = UUID.randomUUID().toString();
        String token = UUID.randomUUID().toString();
        apiKeyDAO.insert(new ApiKeyTable(apiKeyForm.getName(), userIdentified.getUserId(), tokenIdentifier, token, keyPermission));
        actionLogger.user(LogAction.USER_APIKEY_CREATED.create(UserContext.of(userIdentified.getUserId()), "Key Name: " + apiKeyForm.getName() + "<br>" + apiKeyForm.getPermissions().stream().map(NamedPermission::getFrontendName).collect(Collectors.joining(",<br>")), ""));
        return tokenIdentifier + "." + token;
    }

    @Transactional
    public void deleteApiKey(UserIdentified userIdentified, String keyName) {
        if (apiKeyDAO.delete(keyName, userIdentified.getUserId()) == 0) {
            throw new HangarApiException(HttpStatus.NOT_FOUND);
        }
        actionLogger.user(LogAction.USER_APIKEY_DELETED.create(UserContext.of(userIdentified.getUserId()), "", "Key Name: " + keyName));
    }

}
