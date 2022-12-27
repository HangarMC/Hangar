package io.papermc.hangar.model.db.auth;

import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

import java.time.OffsetDateTime;

import io.papermc.hangar.model.db.Table;

public class UserOauthTokenTable extends Table {

    private final String username;
    private final String idToken;

    @JdbiConstructor
    public UserOauthTokenTable(final OffsetDateTime createdAt, final long id, final String username, final String idToken) {
        super(createdAt, id);
        this.username = username;
        this.idToken = idToken;
    }

    public UserOauthTokenTable(final String username, final String idToken) {
        this.username = username;
        this.idToken = idToken;
    }

    public String getIdToken() {
        return this.idToken;
    }

    public String getUsername() {
        return this.username;
    }

    @Override
    public String toString() {
        return "UserOauthTokenTable{" +
               "createdAt=" + this.createdAt +
               ", id=" + this.id +
               ", username='" + this.username + '\'' +
               ", idToken='" + this.idToken + '\'' +
               "} " + super.toString();
    }
}
