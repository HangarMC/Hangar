package io.papermc.hangar.model.db.auth;

import io.papermc.hangar.model.db.Table;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

import java.time.OffsetDateTime;

public class UserSignOnTable extends Table {

    private final String nonce;
    private boolean isCompleted;

    @JdbiConstructor
    public UserSignOnTable(OffsetDateTime createdAt, long id, String nonce, boolean isCompleted) {
        super(createdAt, id);
        this.nonce = nonce;
        this.isCompleted = isCompleted;
    }

    public UserSignOnTable(String nonce) {
        this.nonce = nonce;
    }

    public String getNonce() {
        return nonce;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    @Override
    public String toString() {
        return "UserSignOnTable{" +
                "nonce='" + nonce + '\'' +
                ", isCompleted=" + isCompleted +
                "} " + super.toString();
    }
}
