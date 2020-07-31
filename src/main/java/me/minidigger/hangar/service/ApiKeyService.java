package me.minidigger.hangar.service;

import me.minidigger.hangar.db.dao.ApiKeyDao;
import me.minidigger.hangar.db.dao.HangarDao;
import me.minidigger.hangar.db.model.ApiKeysTable;
import me.minidigger.hangar.model.viewhelpers.ApiKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ApiKeyService {

    private final HangarDao<ApiKeyDao> apiKeyDao;

    @Autowired
    public ApiKeyService(HangarDao<ApiKeyDao> apiKeyDao) {
        this.apiKeyDao = apiKeyDao;
    }

    public List<ApiKey> getKeysForUser(long userId) {
        return apiKeyDao.get().getByOwner(userId).stream().map(ApiKey::new).collect(Collectors.toList());
    }

}
