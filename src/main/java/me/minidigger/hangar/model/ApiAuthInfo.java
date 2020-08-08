package me.minidigger.hangar.model;

import me.minidigger.hangar.db.model.UsersTable;
import me.minidigger.hangar.model.generated.ApiKey;
import org.jdbi.v3.core.mapper.Nested;

import java.time.OffsetDateTime;

public class ApiAuthInfo {

    @Nested("u")
    private UsersTable user;
    @Nested("ak")
    private ApiKey key;
    private String session;
    private OffsetDateTime expires;
    @Nested("gp")
    private Permission globalPerms;

    public UsersTable getUser() {
        return user;
    }

    @Nested("u")
    public void setUser(UsersTable user) {
        this.user = user;
    }

    @Nested("u")
    public ApiKey getKey() {
        return key;
    }

    public void setKey(ApiKey key) {
        this.key = key;
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

    public void setGlobalPerms(Permission globalPerms) {
        this.globalPerms = globalPerms;
    }
}
