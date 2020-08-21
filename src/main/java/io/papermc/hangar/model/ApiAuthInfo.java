package io.papermc.hangar.model;

import io.papermc.hangar.db.model.UsersTable;
import io.papermc.hangar.model.generated.ApiKey;
import org.jdbi.v3.core.mapper.Nested;

import java.time.OffsetDateTime;

public class ApiAuthInfo {

    private UsersTable user;
    private ApiKey key;
    private String session;
    private OffsetDateTime expires;
    private Permission globalPerms;

    public UsersTable getUser() {
        return user;
    }

    @Nested("u")
    public void setUser(UsersTable user) {
        this.user = user.getName() == null ? null : user;
    }

    public ApiKey getKey() {
        return key;
    }

    @Nested("ak")
    public void setKey(ApiKey key) {
        this.key = key.getName() == null ? null : key;
    }

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }

    public OffsetDateTime getExpires() {
        return expires;
    }

    public void setExpires(OffsetDateTime expires) {
        this.expires = expires;
    }

    public Permission getGlobalPerms() {
        return globalPerms;
    }

    @Nested("gp")
    public void setGlobalPerms(Permission globalPerms) {
        this.globalPerms = globalPerms;
    }

    @Override
    public String toString() {
        return "ApiAuthInfo{" +
                "user=" + user +
                ", key=" + key +
                ", session='" + session + '\'' +
                ", expires=" + expires +
                ", globalPerms=" + globalPerms +
                '}';
    }
}
