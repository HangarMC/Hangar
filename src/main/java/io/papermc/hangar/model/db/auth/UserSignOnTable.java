package io.papermc.hangar.model.db.auth;

import io.papermc.hangar.model.db.Table;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

import java.time.OffsetDateTime;

public class UserSignOnTable extends Table {

    private final String nonce;
    private boolean completed;

    @JdbiConstructor
    public UserSignOnTable(OffsetDateTime createdAt, long id, String nonce, boolean completed) {
        super(createdAt, id);
        this.nonce = nonce;
        this.completed = completed;
    }

    public UserSignOnTable(String nonce) {
        this.nonce = nonce;
    }

    public String getNonce() {
        return nonce;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    @Override
    public String toString() {
        return "UserSignOnTable{" +
                "nonce='" + nonce + '\'' +
                ", completed=" + completed +
                "} " + super.toString();
    }
}
