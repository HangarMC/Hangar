package io.papermc.hangar.security.authentication.api;

import io.papermc.hangar.model.common.Permission;
import io.papermc.hangar.model.db.auth.ApiKeyTable;
import io.papermc.hangar.security.authentication.HangarPrincipal;

public class HangarApiPrincipal extends HangarPrincipal {

    private final ApiKeyTable apiKeyTable;

    public HangarApiPrincipal(final long id, final String name, final String email, final boolean locked, final Permission globalPermissions, final ApiKeyTable apiKeyTable, final int aal) {
        super(id, name, email, locked, globalPermissions, null, aal, false);
        this.apiKeyTable = apiKeyTable;
    }

    @Override
    public Permission getPossiblePermissions() {
        return this.apiKeyTable.getPermissions();
    }

    public ApiKeyTable getApiKeyTable() {
        return this.apiKeyTable;
    }

    @Override
    public String toString() {
        return "HangarApiPrincipal{" +
            "apiKeyTable=" + this.apiKeyTable +
            "} " + super.toString();
    }
}
