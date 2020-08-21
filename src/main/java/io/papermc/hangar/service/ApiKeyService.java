package io.papermc.hangar.service;

import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.model.ApiKeysTable;
import io.papermc.hangar.model.viewhelpers.ApiKey;
import io.papermc.hangar.db.dao.ApiKeyDao;
import io.papermc.hangar.model.Permission;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ApiKeyService {

    private final HangarDao<ApiKeyDao> apiKeyDao;

    @Autowired
    public ApiKeyService(HangarDao<ApiKeyDao> apiKeyDao) {
        this.apiKeyDao = apiKeyDao;
    }

    public void createApiKey(String name, long ownerId, String tokenIdentifier, String token, Permission perms) {
        apiKeyDao.get().insert(new ApiKeysTable(name, ownerId, tokenIdentifier, token, perms), perms.getValue());
    }

    public int deleteApiKey(String name, long ownerId) {
        return apiKeyDao.get().delete(name, ownerId);
    }

    public List<ApiKey> getKeys(long userId) {
        return apiKeyDao.get().getByOwner(userId);
    }

    public ApiKeysTable getKey(String keyName, long userId) {
        return apiKeyDao.get().getKey(keyName, userId);
    }

}
