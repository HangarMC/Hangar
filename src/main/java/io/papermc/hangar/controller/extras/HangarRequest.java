package io.papermc.hangar.controller.extras;

import io.papermc.hangar.model.Permission;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.model.db.sessions.ApiKeyTable;
import org.jdbi.v3.core.mapper.Nested;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

import java.time.OffsetDateTime;

public class HangarRequest {

    private final UserTable userTable;
    private final ApiKeyTable apiKeyTable;
    private final String session;
    private final OffsetDateTime expires;
    private final Permission globalPerms;

    @JdbiConstructor
    public HangarRequest(@Nested("u") UserTable userTable, @Nested("ak") ApiKeyTable apiKeyTable, String session, OffsetDateTime expires, Permission globalPerms) {
        this.userTable = userTable;
        this.apiKeyTable = apiKeyTable;
        this.session = session;
        this.expires = expires;
        this.globalPerms = globalPerms;
    }

    public Long getUserId() {
        return userTable == null ? null : userTable.getId();
    }

    public UserTable getUserTable() {
        return userTable;
    }

    public ApiKeyTable getApiKeyTable() {
        return apiKeyTable;
    }

    public String getSession() {
        return session;
    }

    public OffsetDateTime getExpires() {
        return expires;
    }

    public Permission getGlobalPerms() {
        return globalPerms;
    }
}
