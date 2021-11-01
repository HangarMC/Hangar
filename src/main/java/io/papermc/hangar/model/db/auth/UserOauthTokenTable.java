package io.papermc.hangar.model.db.auth;

import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

import java.time.OffsetDateTime;

import io.papermc.hangar.model.db.Table;

public class UserOauthTokenTable extends Table {

    private final String username;
    private final String idToken;

    @JdbiConstructor
    public UserOauthTokenTable(OffsetDateTime createdAt, long id, String username, String idToken) {
        super(createdAt, id);
        this.username = username;
        this.idToken = idToken;
    }

    public UserOauthTokenTable(String username, String idToken) {
        this.username = username;
        this.idToken = idToken;
    }

    public String getIdToken() {
        return idToken;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public String toString() {
        return "UserOauthTokenTable{" +
               "createdAt=" + createdAt +
               ", id=" + id +
               ", username='" + username + '\'' +
               ", idToken='" + idToken + '\'' +
               "} " + super.toString();
    }
}
