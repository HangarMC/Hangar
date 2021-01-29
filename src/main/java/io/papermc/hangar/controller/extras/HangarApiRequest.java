package io.papermc.hangar.controller.extras;

import io.papermc.hangar.model.Permission;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.model.db.sessions.ApiKeyTable;
import org.jdbi.v3.core.mapper.Nested;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

import java.time.OffsetDateTime;

public class HangarApiRequest extends HangarRequest {

    private final ApiKeyTable apiKeyTable;
    private final String session;
    private final OffsetDateTime expires;

    @JdbiConstructor
    public HangarApiRequest(@Nested("u") UserTable userTable, @Nested("ak") ApiKeyTable apiKeyTable, String session, OffsetDateTime expires, Permission globalPermissions) {
        super(userTable, globalPermissions);
        this.apiKeyTable = apiKeyTable;
        this.session = session;
        this.expires = expires;
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

    @Override
    public String toString() {
        return "HangarApiRequest{" +
                "apiKeyTable=" + apiKeyTable +
                ", session='" + session + '\'' +
                ", expires=" + expires +
                "} " + super.toString();
    }
}
