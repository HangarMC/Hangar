package io.papermc.hangar.serviceold;

import io.papermc.hangar.db.dao.HangarDao;
import io.papermc.hangar.db.daoold.ApiKeyDao;
import io.papermc.hangar.db.modelold.ProjectApiKeysTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Deprecated(forRemoval = true)
public class ApiKeyService {

    private final HangarDao<ApiKeyDao> apiKeyDao;

    @Autowired
    public ApiKeyService(HangarDao<ApiKeyDao> apiKeyDao) {
        this.apiKeyDao = apiKeyDao;
    }

    public ProjectApiKeysTable getProjectKey(long keyId) {
        return apiKeyDao.get().getById(keyId);
    }

    public ProjectApiKeysTable createProjectApiKey(ProjectApiKeysTable projectApiKeysTable) {
        return apiKeyDao.get().insert(projectApiKeysTable);
    }

    public void deleteProjectApiKey(ProjectApiKeysTable projectApiKeysTable) {
        apiKeyDao.get().delete(projectApiKeysTable);
    }
}
