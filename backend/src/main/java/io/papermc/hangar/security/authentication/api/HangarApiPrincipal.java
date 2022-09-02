package io.papermc.hangar.security.authentication.api;

import io.papermc.hangar.model.common.Permission;
import io.papermc.hangar.model.db.auth.ApiKeyTable;
import io.papermc.hangar.security.authentication.HangarPrincipal;

public class HangarApiPrincipal extends HangarPrincipal {

    private final ApiKeyTable apiKeyTable;

    public HangarApiPrincipal(long id, String name, boolean locked, Permission globalPermissions, ApiKeyTable apiKeyTable) {
        super(id, name, locked, globalPermissions);
        this.apiKeyTable = apiKeyTable;
    }

    @Override
    public Permission getPossiblePermissions() {
        return apiKeyTable.getPermissions();
    }

    public ApiKeyTable getApiKeyTable() {
        return apiKeyTable;
    }

    @Override
    public String toString() {
        return "HangarApiPrincipal{" +
                "apiKeyTable=" + apiKeyTable +
                "} " + super.toString();
    }
}
